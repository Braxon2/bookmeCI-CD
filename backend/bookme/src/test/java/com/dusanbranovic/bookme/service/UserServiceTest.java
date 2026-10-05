package com.dusanbranovic.bookme.service;

import com.dusanbranovic.bookme.dto.responses.BookableUnitsResponseDTO;
import com.dusanbranovic.bookme.dto.responses.BookingSummaryDTO;
import com.dusanbranovic.bookme.dto.responses.GuestSummaryDTO;
import com.dusanbranovic.bookme.exceptions.EntityNotFoundException;
import com.dusanbranovic.bookme.mappers.BookableUnitMapper;
import com.dusanbranovic.bookme.mappers.UserMapper;
import com.dusanbranovic.bookme.models.BookableUnit;
import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.BookingStatus;
import com.dusanbranovic.bookme.models.User;
import com.dusanbranovic.bookme.repository.BookingRepository;
import com.dusanbranovic.bookme.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private BookableUnitMapper bookableUnitMapper;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks private UserService userService;

    private User guest;

    @BeforeEach
    void setUp() {
        guest = new User();
        guest.setId(4L);
        guest.setEmail("guest@test.com");
        guest.setFirstName("Ana");
        guest.setLastName("Guest");
        guest.setPhoneNumber("123");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUsersMapsEveryUser() {
        User second = new User();
        second.setId(5L);
        GuestSummaryDTO firstDto = new GuestSummaryDTO(4L, "guest@test.com", "Ana", "Guest", "123");
        GuestSummaryDTO secondDto = new GuestSummaryDTO(5L, "second@test.com", "B", "Guest", "456");
        when(userRepository.findAll()).thenReturn(List.of(guest, second));
        when(userMapper.toDTO(guest)).thenReturn(firstDto);
        when(userMapper.toDTO(second)).thenReturn(secondDto);

        assertEquals(List.of(firstDto, secondDto), userService.getUsers());
    }

    @Test
    void getBookingsMapsAndReturnsNewestBookingFirst() {
        BookableUnit unit = new BookableUnit();
        UUID unitId = UUID.randomUUID();
        unit.setPublicId(unitId);
        Booking older = booking(unit, LocalDate.now().minusDays(2));
        Booking newer = booking(unit, LocalDate.now());
        BookableUnitsResponseDTO unitDto = new BookableUnitsResponseDTO(
                unitId, 2, 30, 1, 1, 2, 0, "Room", List.of()
        );
        when(userRepository.findById(4L)).thenReturn(Optional.of(guest));
        when(bookingRepository.findAllByGuestIdWithUnit(4L)).thenReturn(List.of(older, newer));
        when(bookableUnitMapper.toDTO(unit)).thenReturn(unitDto);

        List<BookingSummaryDTO> result = userService.getBookings(4L);

        assertEquals(newer.getPublicId(), result.getFirst().id());
        assertEquals(older.getPublicId(), result.getLast().id());
        org.mockito.Mockito.verify(bookingRepository).completeExpiredBookings(
                org.mockito.ArgumentMatchers.eq(BookingStatus.CONFIRMED),
                org.mockito.ArgumentMatchers.eq(BookingStatus.COMPLETED),
                org.mockito.ArgumentMatchers.any(java.time.LocalDateTime.class)
        );
    }

    @Test
    void getBookingsRejectsAnUnknownUser() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getBookings(404L));
    }

    @Test
    void getUserSummaryReturnsTheAuthenticatedUsersData() {
        authenticate(guest);
        when(userRepository.findById(4L)).thenReturn(Optional.of(guest));

        GuestSummaryDTO result = userService.getUserSummary(4L);

        assertEquals("guest@test.com", result.email());
        assertEquals("Ana", result.firstName());
    }

    @Test
    void getUserSummaryRejectsAccessToAnotherUser() {
        User currentUser = new User();
        currentUser.setId(9L);
        authenticate(currentUser);
        when(userRepository.findById(4L)).thenReturn(Optional.of(guest));

        assertThrows(AccessDeniedException.class, () -> userService.getUserSummary(4L));
    }

    private Booking booking(BookableUnit unit, LocalDate createdAt) {
        return new Booking(
                unit,
                guest,
                100.0,
                createdAt,
                createdAt.plusDays(1).atStartOfDay(),
                createdAt.plusDays(2).atStartOfDay(),
                BookingStatus.CONFIRMED
        );
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        );
    }
}
