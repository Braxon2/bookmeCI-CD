package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class EntityAlreadyExistsExcpetion extends BaseException{

    public EntityAlreadyExistsExcpetion(String message) {
        super(message, "ENTITY_ALREADY_EXISTS" ,HttpStatus.CONFLICT);
    }
}
