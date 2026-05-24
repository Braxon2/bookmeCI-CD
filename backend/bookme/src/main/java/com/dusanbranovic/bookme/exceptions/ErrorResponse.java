package com.dusanbranovic.bookme.exceptions;

import java.time.Instant;

public class ErrorResponse {
    private String errorCode;
    private String message;
    private int status;
    private Instant timestamp;
    private String path;

    public ErrorResponse(String errorCode, String message, int status, Instant timestamp, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.path = path;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}
