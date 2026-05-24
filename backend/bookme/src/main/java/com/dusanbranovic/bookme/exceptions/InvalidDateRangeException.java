package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidDateRangeException extends BaseException {
    public InvalidDateRangeException(String message) {
        super(message, "INVALID_DATE_RANGE" , HttpStatus.CONFLICT);
    }
}
