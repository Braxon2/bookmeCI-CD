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
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookableUnitRepository bookableUnitRepository;
    private final AddonRepository addonRepository;
    private final AddonMappingRepository addonMappingRepository;

    private final BookableUnitMapper bookableUnitMapper;
    private final PricingService pricingService;

    @Value("${booking.time-zone:Europe/Belgrade}")
    private String bookingTimeZone = "Europe/Belgrade";

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);


    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BookableUnitRepository bookableUnitRepository,
            AddonRepository addonRepository,
            AddonMappingRepository addonMappingRepository,
            BookableUnitMapper bookableUnitMapper,
            PricingService pricingService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.bookableUnitRepository = bookableUnitRepository;
        this.addonRepository = addonRepository;
        this.addonMappingRepository = addonMappingRepository;
        this.bookableUnitMapper = bookableUnitMapper;
        this.pricingService = pricingService;
    }

    @Transactional
    public BookingResponseDTO bookAUnit(
            UUID unitId,
            BookingRequestDTO bookingRequestDTO
    ) {


        BookableUnit unit = bookableUnitRepository
                .findByPublicId(unitId)
                .orElseThrow(() -> {
                    log.error("Unit with id {} not found", unitId);
                    return new EntityNotFoundException(
                            "Unit with id " + unitId + " not found"
                    );
                });

        LocalDate start = bookingRequestDTO.start_date();
        LocalDate end = bookingRequestDTO.end_date();

        if (start == null || end == null || !start.isBefore(end)) {
            log.error("Invalid booking date range: {} - {}", start, end);
            throw new InvalidDateRangeException("Start date must be before end date");
        }


        LocalDateTime checkIn = start.atStartOfDay();
        LocalDateTime checkOut = end.atStartOfDay();


        long overlappingCount = bookingRepository.countOverlappingBookings(unitId, checkIn,checkOut);

        if (overlappingCount >= unit.getTotalUnits()) {
            log.error("No available units for unit {} between {} and {}", unitId, start, end);
            throw new OverlappingBookingExcpetion("No available units for selected dates");
        }


        List<AddonMapping> addonMappings = new ArrayList<>();

        if (bookingRequestDTO.addons() != null) {

            List<Long> addonIds =
                    bookingRequestDTO
                            .addons()
                            .stream()
                            .map(AddonsRequestDTO::id)
                            .distinct()
                            .toList();

            for (Long addonId : addonIds) {

                AddonMapping addonMapping =
                        addonMappingRepository
                                .findAvailableAddonForPeriod(
                                        unitId,
                                        addonId,
                                        start,
                                        end
                                )
                                .orElseThrow(() -> {

                                    log.error(
                                            "Addon {} is not available for unit {} between {} and {}",
                                            addonId,
                                            unitId,
                                            start,
                                            end
                                    );

                                    return new EntityNotFoundException(
                                            "Addon with id "
                                                    + addonId
                                                    + " is not available for this unit "
                                                    + "during the selected dates"
                                    );
                                });

                addonMappings.add(addonMapping);
            }
        }


        List<PeriodPrice> prices = unit.getPeriodPriceList();

        double totalPrice =
                pricingService.calculateUnitPrice(
                        start,
                        end,
                        prices
                );


        double totalAddonPrice = 0.0;

        for (AddonMapping mapping : addonMappings) {

            double addonPrice =
                    pricingService.calculateAddonPrice(
                            start,
                            end,
                            mapping
                    );

            totalAddonPrice += addonPrice;
        }

        totalPrice += totalAddonPrice;

        log.info(
                "Total booking price calculated successfully: {}",
                totalPrice
        );




        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

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

            double historicalPriceForThisAddon =
                    pricingService.calculateAddonPrice(
                            start,
                            end,
                            mapping
                    );

            BookingAddonItem item =
                    new BookingAddonItem(
                            booking,
                            mapping.getAddon().getName(),
                            historicalPriceForThisAddon,
                            mapping.isPerNight(),
                            mapping.getAddon()
                    );

            booking.addAddonItem(item);
        }

        Booking savedBooking =
                bookingRepository.save(booking);

        log.info(
                "Booking {} created successfully",
                savedBooking.getPublicId()
        );


        BookableUnitsResponseDTO unitDTO =
                bookableUnitMapper.toDTO(unit);

        GuestSummaryDTO guestDTO =
                new GuestSummaryDTO(
                        guest.getId(),
                        guest.getEmail(),
                        guest.getFirstName(),
                        guest.getLastName(),
                        guest.getPhoneNumber()
                );


        return new BookingResponseDTO(
                savedBooking.getPublicId(),
                unitDTO,
                guestDTO,
                totalPrice,
                savedBooking.getCreatedAt(),
                savedBooking.getCheckIn(),
                savedBooking.getCheckOut(),
                savedBooking.getStatus()
        );
    }


    @Transactional
    public BookingSummaryDTO cancelBooking(UUID bookingID) {
        Booking bookingToCancel = bookingRepository.findByPublicId(bookingID).orElseThrow(() ->{
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

        LocalDate today = LocalDate.now(ZoneId.of(bookingTimeZone));
        LocalDate cancellationCutoff = bookingToCancel.getCheckIn().toLocalDate().minusDays(1);
        if (!today.isBefore(cancellationCutoff)) {
            log.warn("User {} attempted to cancel booking {} after its cancellation cutoff",
                    currentUser.getId(), bookingID);
            throw new InvalidBookingStateException(
                    "Bookings cannot be cancelled on the day before check-in or later."
            );
        }

        bookingToCancel.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(bookingToCancel);

        log.info("Booking {} successfully cancelled by user {}", bookingID, currentUser.getId());

        return new BookingSummaryDTO(
                savedBooking.getPublicId(),
                bookableUnitMapper.toDTO(bookingToCancel.getBookableUnit()),
                bookingToCancel.getTotalPrice(),
                bookingToCancel.getCreatedAt(),
                bookingToCancel.getCheckIn(),
                bookingToCancel.getCheckOut(),
                bookingToCancel.getStatus());
    }
}
