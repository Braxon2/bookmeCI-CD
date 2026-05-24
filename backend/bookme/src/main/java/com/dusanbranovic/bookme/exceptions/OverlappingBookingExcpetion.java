package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class OverlappingBookingExcpetion extends BaseException {
    public OverlappingBookingExcpetion(String message) {
        super(message, "OVERLAPPING_BOOKINGS" , HttpStatus.CONFLICT);
    }
}
