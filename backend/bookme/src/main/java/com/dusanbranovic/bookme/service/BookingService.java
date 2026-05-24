package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddonsRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.requests.BookingRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookingResponseDTO;
import com.dusanbranovic.bookme.dto.responses.BookingSummaryDTO;
import com.dusanbranovic.bookme.dto.responses.GuestSummaryDTO;
import com.dusanbranovic.bookme.exceptions.*;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.models.*;
import com.dusanbranovic.bookme.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookableUnitRepository bookableUnitRepository;
    private final AddonRepository addonRepository;
    private final AddonMappingRepository addonMappingRepository;

    private final BookableUnitMapper bookableUnitMapper;

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);


    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BookableUnitRepository bookableUnitRepository,
            AddonRepository addonRepository,
            AddonMappingRepository addonMappingRepository,
            BookableUnitMapper bookableUnitMapper
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.bookableUnitRepository = bookableUnitRepository;
        this.addonRepository = addonRepository;
        this.addonMappingRepository = addonMappingRepository;
        this.bookableUnitMapper = bookableUnitMapper;
    }

    public BookingResponseDTO bookAUnit(Long unitId, BookingRequestDTO bookingRequestDTO) {
        BookableUnit unit = bookableUnitRepository.findById(unitId).orElseThrow(() -> {
            log.error("Unit not found");
            return new EntityNotFoundException("Unit with id " + unitId + " not found");
        });


        List<AddonMapping> addonMappings = new ArrayList<>();

        if(bookingRequestDTO.addons() != null) {
            List<Long> addonIds = bookingRequestDTO.addons().stream().map(AddonsRequestDTO::id).toList();

            for (int i = 0; i < addonIds.size(); i++) {
                int row = i;

                Long addonId = addonIds.get(i);
                Addon addon = addonRepository.findById(addonId).orElseThrow(() -> {
                    log.error("Addon not found");
                    throw new EntityNotFoundException("Addon with id " + addonIds.get(row) + " not found");
                });

                AddonMapping addonMapping = addonMappingRepository.findByAddonAndUnitID(unitId,addonId).orElseThrow(() ->{
                    log.error("Addon not found in unit");
                    return new EntityNotFoundException("Addon with id " + addonId + " not found in unit with id " + unitId);
                });

                addonMappings.add(addonMapping);
            }
        }


        LocalDate start = bookingRequestDTO.start_date();
        LocalDate end = bookingRequestDTO.end_date();

        if (!start.isBefore(end)) {
            log.error("Start date must be before end date");
            throw new InvalidDateRangeException("Start date must be before end date");
        }

        LocalDateTime checkIn = start.atStartOfDay();
        LocalDateTime checkOut = end.atStartOfDay();

        long overlappingCount =
                bookingRepository.countOverlappingBookings(unitId, checkIn, checkOut);

        if (overlappingCount >= unit.getTotalUnits()) {
            log.error("No available units for selected dates");
            throw new OverlappingBookingExcpetion("No available units for selected dates");
        }

        List<PeriodPrice> prices = unit.getPeriodPriceList();
        double totalAddonPrice = 0;
        double totalPrice = calculatePrice(start, end, prices);

        for (int i = 0; i < addonMappings.size(); i++) {
            totalAddonPrice += calculateAddonPrice(start, end, addonMappings.get(i));
        }

        totalPrice += totalAddonPrice;

        log.info("Total price calculated successfully");


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User guest = (User) auth.getPrincipal();

        Booking booking = new Booking(
                unit,
                guest,
                totalPrice,
                LocalDate.now(),
                checkIn,
                checkOut,
                BookingStatus.CONFIRMED
        );

        for (AddonMapping mapping : addonMappings) {
            double historicalPriceForThisAddon = calculateAddonPrice(start, end, mapping);

            BookingAddonItem item = new BookingAddonItem(
                    booking,
                    mapping.getAddon(),
                    historicalPriceForThisAddon,
                    mapping.isPerNight()
            );

            booking.addAddonItem(item);
        }

        log.info("Booking created successfully");

        Booking savedBooking = bookingRepository.save(booking);


        BookableUnitsResponseDTO unitDTO = bookableUnitMapper.toDTO(unit);

        GuestSummaryDTO guestDTO = new GuestSummaryDTO(
                guest.getId(),
                guest.getEmail(),
                guest.getFirstName(),
                guest.getLastName(),
                guest.getPhoneNumber()
        );

        return new BookingResponseDTO(
                savedBooking.getId(),
                unitDTO,
                guestDTO,
                totalPrice,
                LocalDate.now(),
                checkIn,
                checkOut,
                BookingStatus.CONFIRMED
        );
    }


    private double calculatePrice(
            LocalDate start,
            LocalDate end,
            List<PeriodPrice> prices
    ) {

        double totalPrice = 0.0;
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {

            LocalDate finalDate = date;
            PeriodPrice priceForDay = prices.stream()
                    .filter(p ->
                            !finalDate.isBefore(p.getStartDate()) &&
                                    !finalDate.isAfter(p.getEndDate())
                    )
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("No price defined for date " + finalDate);
                        return new EntityNotFoundException(
                                "No price defined for date " + finalDate);
                    });


            totalPrice += priceForDay.getPricePerNight();
        }

        return totalPrice;
    }

    private double calculateAddonPrice(
            LocalDate start,
            LocalDate end,
            AddonMapping addonMapping
    ) {
        List<PeriodPriceAddon> prices = addonMapping.getPeriodPriceAddons();

        if (addonMapping.isPerNight()) {
            return calculatePerNight(start, end, prices);
        } else {
            return calculateOnce(start, prices);
        }
    }

    private double calculateOnce(
            LocalDate start,
            List<PeriodPriceAddon> prices
    ) {
        PeriodPriceAddon price = prices.stream()
                .filter(p ->
                        !start.isBefore(p.getStartDate()) &&
                                !start.isAfter(p.getEndDate())
                )
                .reduce((firsy, second) -> second)
                .orElseThrow(() -> {
                    log.warn("No addon price defined");
                    return new EntityNotFoundException("No addon price defined");
                });

        return price.getPrice();
    }

    private double calculatePerNight(
            LocalDate start,
            LocalDate end,
            List<PeriodPriceAddon> prices
    ) {

        double totalPrice = 0.0;
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {

            LocalDate finalDate = date;
            PeriodPriceAddon priceForDay = prices.stream()
                    .filter(p ->
                            !finalDate.isBefore(p.getStartDate()) &&
                                    !finalDate.isAfter(p.getEndDate())
                    )
                    .findFirst()
                    .orElseThrow(() -> {
                                log.warn("No price defined for date " + finalDate);
                                return new EntityNotFoundException(
                                        "No price defined for date " + finalDate);
                            }
                    );

            totalPrice += priceForDay.getPrice();
        }

        return totalPrice;
    }

    @Transactional
    public BookingSummaryDTO cancelBooking(Long bookingID) {
        Booking bookingToCancel = bookingRepository.findById(bookingID).orElseThrow(() ->{
            log.error("Booking not found");
            return new EntityNotFoundException("Booking with " + bookingID + " not found");
        });

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (!bookingToCancel.getGuest().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to cancel booking {} owned by user {}",
                    currentUser.getId(), bookingID, bookingToCancel.getGuest().getId());
            throw new AccessDeniedException("You do not have permission to cancel this booking.");
        }

        if (bookingToCancel.getStatus() == BookingStatus.CANCELLED) {
            log.warn("User {} attempted to cancel booking {} which is already cancelled", currentUser.getId(), bookingID);
            throw new InvalidBookingStateException("Booking is already cancelled.");
        }
        if (bookingToCancel.getStatus() == BookingStatus.COMPLETED) {
            log.warn("User {} attempted to cancel booking {} which is already completed", currentUser.getId(), bookingID);
            throw new InvalidBookingStateException("Cannot cancel a completed booking.");
        }

        bookingToCancel.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(bookingToCancel);

        log.info("Booking {} successfully cancelled by user {}", bookingID, currentUser.getId());

        return new BookingSummaryDTO(
                savedBooking.getId(),
                bookableUnitMapper.toDTO(bookingToCancel.getBookableUnit()),
                bookingToCancel.getTotalPrice(),
                bookingToCancel.getCreatedAt(),
                bookingToCancel.getCheckIn(),
                bookingToCancel.getCheckOut(),
                bookingToCancel.getStatus());
    }
}
