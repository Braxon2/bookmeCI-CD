package com.dusanbranovic.bookme.ai.exception;

import org.springframework.http.HttpStatus;

public class UnitSearchException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public UnitSearchException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() { return status; }
    public String code() { return code; }

    public static UnitSearchException invalid(String message) {
        return new UnitSearchException(HttpStatus.BAD_REQUEST, "AI_INVALID_ARGUMENT", message);
    }
}
