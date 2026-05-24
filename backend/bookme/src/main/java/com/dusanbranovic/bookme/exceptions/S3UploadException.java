package com.dusanbranovic.bookme.exceptions;

import org.springframework.http.HttpStatus;

public class S3UploadException extends BaseException{
    public S3UploadException(String message) {
        super(message, "FILE_UPLOAD_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
