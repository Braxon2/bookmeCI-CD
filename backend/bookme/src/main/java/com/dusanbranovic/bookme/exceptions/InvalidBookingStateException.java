package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidBookingStateException extends BaseException {
  public InvalidBookingStateException(String message) {
    super(message, "INVALID_BOOKING_STATE" , HttpStatus.CONFLICT);
  }
}
