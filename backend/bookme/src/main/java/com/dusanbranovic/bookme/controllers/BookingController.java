package com.dusanbranovic.bookme.controllers;


import com.dusanbranovic.bookme.dto.responses.BookingSummaryDTO;
import com.dusanbranovic.bookme.service.BookingService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PatchMapping("/{bookingID}")
    public BookingSummaryDTO cancelBooking(@PathVariable Long bookingID){
        return bookingService.cancelBooking(bookingID);
    }
}
