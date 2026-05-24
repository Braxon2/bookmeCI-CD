package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.requests.AddonsRequestDTO;
import com.dusanbranovic.bookme.dto.requests.BookingRequestDTO;
import com.dusanbranovic.bookme.dto.responses.BookingResponseDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.exceptions.InvalidDateRangeException;
import com.dusanbranovic.bookme.exceptions.OverlappingBookingExcpetion;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.PeriodPrice;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.AddonRepository;
import com.dusanbranovic.bookme.repository.BookableUnitRepository;
import com.dusanbranovic.bookme.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookableUnitRepository bookableUnitRepository;
    @Mock
    private AddonRepository addonRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void bookAUnit_unitNotFound_throwsException() {
        when(bookableUnitRepository.findById(1L)).thenReturn(Optional.empty());

        BookingRequestDTO dto = new BookingRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                List.of()
        );

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.bookAUnit(1L, dto));
    }

    @Test
    void bookAUnit_startAfterEnd_throwsException() {
        BookableUnit unit = new BookableUnit();
        when(bookableUnitRepository.findById(1L)).thenReturn(Optional.of(unit));

        BookingRequestDTO dto = new BookingRequestDTO(
                LocalDate.now(),
                LocalDate.now(),
                List.of()
        );

        assertThrows(InvalidDateRangeException.class,
                () -> bookingService.bookAUnit(1L, dto));
    }

    @Test
    void bookAUnit_noAvailableUnits_throwsException() {
        BookableUnit unit = new BookableUnit();
        unit.setTotalUnits(1);

        when(bookableUnitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(bookingRepository.countOverlappingBookings(
                anyLong(), any(), any()))
                .thenReturn(1L);

        BookingRequestDTO dto = new BookingRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                List.of()
        );

        assertThrows(OverlappingBookingExcpetion.class,
                () -> bookingService.bookAUnit(1L, dto));
    }

    @Test
    void bookAUnit_addonNotFound_throwsException() {
        BookableUnit unit = new BookableUnit();
        when(bookableUnitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(addonRepository.findById(10L)).thenReturn(Optional.empty());

        BookingRequestDTO dto = new BookingRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                List.of(new AddonsRequestDTO(10L))
        );

        assertThrows(EntityNotFoundException.class,
                () -> bookingService.bookAUnit(1L, dto));
    }

    @Test
    void bookAUnit_validData_createsBooking() {
        BookableUnit unit = new BookableUnit();
        unit.setId(1L);
        unit.setTotalUnits(2);

        PeriodPrice price = new PeriodPrice(
                unit, 100,
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "SUMMER"
        );
        unit.setPeriodPriceList(List.of(price));

        User user = new User();

        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(context);

        when(bookableUnitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.countOverlappingBookings(any(), any(), any()))
                .thenReturn(0L);

        BookingRequestDTO dto = new BookingRequestDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                List.of()
        );

        BookingResponseDTO response =
                bookingService.bookAUnit(1L, dto);

        assertEquals(200, response.totalPrice());
        verify(bookingRepository).save(any(Booking.class));
    }

}