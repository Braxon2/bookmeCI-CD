package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.BookingStatus;
import com.dusanbranovic.bookme.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends AbstractControllerIT {

    @Autowired private BookingRepository bookingRepository;

    @Test
    void guestCanReadOwnSummary() throws Exception {
        mockMvc.perform(get("/api/users/{id}", guest.getId()).with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guest.getId()))
                .andExpect(jsonPath("$.email").value(guest.getEmail()))
                .andExpect(jsonPath("$.firstName").value("Test"));
    }

    @Test
    void guestCanReadOwnBookings() throws Exception {
        LocalDate start = LocalDate.now().plusDays(2);
        Booking booking = bookingRepository.save(new Booking(
                unit,
                guest,
                240.0,
                LocalDate.now(),
                start.atStartOfDay(),
                start.plusDays(2).atStartOfDay(),
                BookingStatus.CONFIRMED
        ));

        mockMvc.perform(get("/api/users/{id}/bookings", guest.getId()).with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getPublicId().toString()))
                .andExpect(jsonPath("$[0].totalPrice").value(240.0));
    }

    @Test
    void ownerCanReadOwnedProperties() throws Exception {
        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/api/users/{id}/properties", owner.getId()).with(asOwner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(property.getPublicId().toString()));
    }

    @Test
    void guestCannotReadAnotherUsersSummary() throws Exception {
        mockMvc.perform(get("/api/users/{id}", owner.getId()).with(asGuest()))
                .andExpect(status().isForbidden());
    }
}
