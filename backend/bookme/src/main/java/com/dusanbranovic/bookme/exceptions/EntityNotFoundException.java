package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BaseException{
    public EntityNotFoundException(String message) {
        super(message, "ENTITY_NOT_FOUND" ,HttpStatus.NOT_FOUND);
    }
}
