package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidPriceValueException extends BaseException {
    public InvalidPriceValueException(String message) {
        super(message,"INVALID_PRICE_VALUE", HttpStatus.CONFLICT);
    }
}
