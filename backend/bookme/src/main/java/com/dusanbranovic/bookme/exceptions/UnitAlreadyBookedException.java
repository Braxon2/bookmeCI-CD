package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class UnitAlreadyBookedException extends BaseException{
    public UnitAlreadyBookedException(String message) {
        super(message, "UNIT_ALREADY_BOOKED" ,HttpStatus.CONFLICT);
    }
}
