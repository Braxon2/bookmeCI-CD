package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.BookingStatus;
import com.dusanbranovic.bookme.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingControllerIT extends AbstractControllerIT {

    @Autowired private BookingRepository bookingRepository;

    @Test
    void guestCanCancelOwnConfirmedBooking() throws Exception {
        Booking booking = bookingRepository.save(booking(BookingStatus.CONFIRMED));

        mockMvc.perform(patch("/api/bookings/{id}", booking.getPublicId()).with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getPublicId().toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(BookingStatus.CANCELLED, bookingRepository.findById(booking.getId()).orElseThrow().getStatus());
    }

    @Test
    void completedBookingCannotBeCancelled() throws Exception {
        Booking booking = bookingRepository.save(booking(BookingStatus.COMPLETED));

        mockMvc.perform(patch("/api/bookings/{id}", booking.getPublicId()).with(asGuest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot cancel a completed booking."));
    }

    @Test
    void ownerCannotUseGuestCancellationEndpoint() throws Exception {
        Booking booking = bookingRepository.save(booking(BookingStatus.CONFIRMED));

        mockMvc.perform(patch("/api/bookings/{id}", booking.getPublicId()).with(asOwner()))
                .andExpect(status().isForbidden());
    }

    @Test
    void bookingCannotBeCancelledOnTheDayBeforeCheckIn() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        Booking booking = bookingRepository.save(new Booking(
                unit,
                guest,
                200.0,
                LocalDate.now(),
                start.atStartOfDay(),
                start.plusDays(2).atStartOfDay(),
                BookingStatus.CONFIRMED
        ));

        mockMvc.perform(patch("/api/bookings/{id}", booking.getPublicId()).with(asGuest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Bookings cannot be cancelled on the day before check-in or later."
                ));

        assertEquals(
                BookingStatus.CONFIRMED,
                bookingRepository.findByPublicId(booking.getPublicId()).orElseThrow().getStatus()
        );
    }

    private Booking booking(BookingStatus status) {
        LocalDate start = LocalDate.now().plusDays(2);
        return new Booking(
                unit,
                guest,
                200.0,
                LocalDate.now(),
                start.atStartOfDay(),
                start.plusDays(2).atStartOfDay(),
                status
        );
    }
}
