package com.dusanbranovic.bookme.integrations.contollers;

import com.dusanbranovic.bookme.models.Booking;
import com.dusanbranovic.bookme.models.BookingStatus;
import com.dusanbranovic.bookme.repository.BookingRepository;
import com.dusanbranovic.bookme.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerIT extends AbstractControllerIT {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private ReviewRepository reviewRepository;

    @Test
    void guestCanReviewCompletedBookingAndReviewsAppearForUnitAndProperty() throws Exception {
        Booking booking = bookingRepository.save(completedBooking());

        mockMvc.perform(post("/api/reviews/bookings/{id}/reviews", booking.getPublicId())
                        .with(asGuest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"text\":\"Wonderful and quiet stay\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewer.firstName").value("Test"))
                .andExpect(jsonPath("$.bookableUnitPublicId").value(unit.getPublicId().toString()));

        assertTrue(reviewRepository.existsByBooking_Id(booking.getId()));
        mockMvc.perform(get("/api/reviews/units/{id}/reviews", unit.getPublicId()).with(asGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].text").value("Wonderful and quiet stay"));
        mockMvc.perform(get("/api/reviews/properties/{id}/reviews", property.getPublicId()).with(asOwner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void confirmedBookingCannotBeReviewed() throws Exception {
        Booking booking = completedBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        mockMvc.perform(post("/api/reviews/bookings/{id}/reviews", booking.getPublicId())
                        .with(asGuest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"text\":\"Not completed yet\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only completed bookings can be reviewed"));
    }

    @Test
    void invalidRatingReturnsBadRequest() throws Exception {
        Booking booking = bookingRepository.save(completedBooking());

        mockMvc.perform(post("/api/reviews/bookings/{id}/reviews", booking.getPublicId())
                        .with(asGuest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":6,\"text\":\"Invalid rating\"}"))
                .andExpect(status().isBadRequest());
    }

    private Booking completedBooking() {
        LocalDate start = LocalDate.now().minusDays(4);
        return new Booking(
                unit,
                guest,
                180.0,
                start.minusDays(2),
                start.atStartOfDay(),
                start.plusDays(2).atStartOfDay(),
                BookingStatus.COMPLETED
        );
    }
}
