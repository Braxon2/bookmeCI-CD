package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class ReviewNotAllowedException extends BaseException {
    public ReviewNotAllowedException(String message) {
        super(message, "INALID_REVIEW_EXCEPTION" , HttpStatus.CONFLICT);
    }
}
