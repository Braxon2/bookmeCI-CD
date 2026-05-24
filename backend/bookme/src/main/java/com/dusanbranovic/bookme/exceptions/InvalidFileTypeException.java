package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends BaseException {
    public InvalidFileTypeException(String message) {
        super(message, "INVALID_FILE_TYPE" , HttpStatus.BAD_REQUEST);
    }
}
