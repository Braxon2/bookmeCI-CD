package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddonsRequestDTO;
import com.dusanbranovic.bookme.dto.requests.BookingRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.BookingResponseDTO;
import com.dusanbranovic.bookme.dto.responses.BookingSummaryDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidBookingStateException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.exceptions.OverlappingBookingExcpetion;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.models.Addon;
import com.dusanbranovic.bookme.models.AddonMapping;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.BookingStatus;
import com.dusanbranovic.bookme.models.PeriodPrice;
import com.dusanbranovic.bookme.models.PeriodPriceAddon;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.AddonMappingRepository;
import com.dusanbranovic.bookme.repository.AddonRepository;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.BookingRepository;
import com.dusanbranovic.bookme.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private AddonRepository addonRepository;
    @Mock
    private AddonMappingRepository addonMappingRepository;
    @Mock
    private BookableUnitMapper bookableUnitMapper;
    @Spy
    private PricingService pricingService = new PricingService();

    @InjectMocks
    private BookingService bookingService;

    private UUID unitId;
    private BookableUnit unit;
    private User guest;
    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();
        unit = new BookableUnit();
        unit.setId(10L);
        unit.setPublicId(unitId);
        unit.setName("Deluxe room");
        unit.setTotalUnits(2);
        start = LocalDate.now().plusDays(2);
        end = start.plusDays(2);
        unit.setPeriodPriceList(List.of(
                new PeriodPrice(unit, 100, start, end, "Standard")
        ));
        guest = new User();
        guest.setId(44L);
        guest.setEmail("guest@test.com");
        guest.setFirstName("Test");
        guest.setLastName("Guest");
        setCurrentUser(guest);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void bookAUnitRejectsAnUnknownUnit() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.bookAUnit(unitId, request(start, end, List.of()))
        );
    }

    @Test
    void bookAUnitRejectsInvalidDates() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));

        assertThrows(
                InvalidDateRangeException.class,
                () -> bookingService.bookAUnit(unitId, request(start, start, List.of()))
        );
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bookAUnitRejectsACompletelyBookedUnit() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.countOverlappingBookings(
                unitId, start.atStartOfDay(), end.atStartOfDay()
        )).thenReturn(2L);

        assertThrows(
                OverlappingBookingExcpetion.class,
                () -> bookingService.bookAUnit(unitId, request(start, end, List.of()))
        );
    }

    @Test
    void bookAUnitRejectsAnUnavailableAddon() {
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.countOverlappingBookings(any(), any(), any())).thenReturn(0L);
        when(addonMappingRepository.findAvailableAddonForPeriod(unitId, 7L, start, end))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.bookAUnit(
                        unitId,
                        request(start, end, List.of(new AddonsRequestDTO(7L)))
                )
        );
    }

    @Test
    void bookAUnitCalculatesNightlyAndAddonPricesAndSavesSnapshots() {
        Addon breakfast = new Addon("Breakfast");
        breakfast.setId(7L);
        AddonMapping mapping = new AddonMapping(true, unit, breakfast, start.minusDays(1));
        mapping.setPeriodPriceAddons(List.of(new PeriodPriceAddon(mapping, 10, start, end)));
        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.countOverlappingBookings(any(), any(), any())).thenReturn(0L);
        when(addonMappingRepository.findAvailableAddonForPeriod(unitId, 7L, start, end))
                .thenReturn(Optional.of(mapping));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookableUnitMapper.toDTO(unit)).thenReturn(unitDTO());

        BookingResponseDTO result = bookingService.bookAUnit(
                unitId,
                request(start, end, List.of(new AddonsRequestDTO(7L), new AddonsRequestDTO(7L)))
        );

        assertEquals(220, result.totalPrice());
        assertEquals(BookingStatus.CONFIRMED, result.status());
        org.mockito.ArgumentCaptor<Booking> captor = org.mockito.ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getAddonItems().size());
        assertEquals("Breakfast", captor.getValue().getAddonItems().getFirst().getAddonNameSnapshot());
        assertEquals(20, captor.getValue().getAddonItems().getFirst().getPricePaid());
    }

    @Test
    void bookAUnitUsesNewerOverlappingUnitAndAddonPrices() {
        end = start.plusDays(3);
        PeriodPrice october = new PeriodPrice(unit, 70, start.minusDays(8), end.plusDays(10), "October");
        october.setId(1L);
        PeriodPrice special = new PeriodPrice(unit, 100, start.minusDays(1), start.plusDays(1), "Special");
        special.setId(2L);
        unit.setPeriodPriceList(List.of(special, october));

        Addon breakfast = new Addon("Breakfast");
        breakfast.setId(7L);
        AddonMapping mapping = new AddonMapping(true, unit, breakfast, start.minusDays(10));
        PeriodPriceAddon regularAddonPrice = new PeriodPriceAddon(
                mapping, 10, start.minusDays(8), end.plusDays(10)
        );
        regularAddonPrice.setId(1L);
        PeriodPriceAddon specialAddonPrice = new PeriodPriceAddon(
                mapping, 20, start.minusDays(1), start.plusDays(1)
        );
        specialAddonPrice.setId(2L);
        mapping.setPeriodPriceAddons(List.of(specialAddonPrice, regularAddonPrice));

        when(bookableUnitRepository.findByPublicId(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.countOverlappingBookings(any(), any(), any())).thenReturn(0L);
        when(addonMappingRepository.findAvailableAddonForPeriod(unitId, 7L, start, end))
                .thenReturn(Optional.of(mapping));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookableUnitMapper.toDTO(unit)).thenReturn(unitDTO());

        BookingResponseDTO result = bookingService.bookAUnit(
                unitId,
                request(start, end, List.of(new AddonsRequestDTO(7L)))
        );

        assertEquals(320.0, result.totalPrice());
        org.mockito.ArgumentCaptor<Booking> captor = org.mockito.ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(50.0, captor.getValue().getAddonItems().getFirst().getPricePaid());
    }

    @Test
    void cancelBookingChangesStatusForTheOwningGuest() {
        Booking booking = booking(guest, BookingStatus.CONFIRMED);
        UUID bookingId = booking.getPublicId();
        when(bookingRepository.findByPublicId(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookableUnitMapper.toDTO(unit)).thenReturn(unitDTO());

        BookingSummaryDTO result = bookingService.cancelBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, result.status());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBookingRejectsAnotherGuestsBooking() {
        User ownerOfBooking = new User();
        ownerOfBooking.setId(99L);
        Booking booking = booking(ownerOfBooking, BookingStatus.CONFIRMED);
        UUID bookingId = booking.getPublicId();
        when(bookingRepository.findByPublicId(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(bookingId));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBookingRejectsAnAlreadyCancelledBooking() {
        Booking booking = booking(guest, BookingStatus.CANCELLED);
        UUID bookingId = booking.getPublicId();
        when(bookingRepository.findByPublicId(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(InvalidBookingStateException.class, () -> bookingService.cancelBooking(bookingId));
    }

    @Test
    void cancelBookingRejectsCancellationOnTheDayBeforeCheckIn() {
        Booking booking = booking(guest, BookingStatus.CONFIRMED);
        booking.setCheckIn(LocalDate.now().plusDays(1).atStartOfDay());
        booking.setCheckOut(LocalDate.now().plusDays(3).atStartOfDay());
        UUID bookingId = booking.getPublicId();
        when(bookingRepository.findByPublicId(bookingId)).thenReturn(Optional.of(booking));

        InvalidBookingStateException exception = assertThrows(
                InvalidBookingStateException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        assertEquals(
                "Bookings cannot be cancelled on the day before check-in or later.",
                exception.getMessage()
        );
        verify(bookingRepository, never()).save(any());
    }

    private BookingRequestDTO request(LocalDate from, LocalDate to, List<AddonsRequestDTO> addons) {
        return new BookingRequestDTO(from, to, addons);
    }

    private BookableUnitsResponseDTO unitDTO() {
        return new BookableUnitsResponseDTO(unitId, 4, 40, 2, 1, 3, 1, "Deluxe room", List.of());
    }

    private Booking booking(User bookingGuest, BookingStatus status) {
        Booking booking = new Booking(
                unit,
                bookingGuest,
                200.0,
                LocalDate.now(),
                start.atStartOfDay(),
                end.atStartOfDay(),
                status
        );
        booking.setId(1L);
        return booking;
    }

    private void setCurrentUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        );
    }
}
