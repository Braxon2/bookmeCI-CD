package com.dusanbranovic.bookme.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    }

    @ExceptionHandler(UnitAlreadyBookedException.class)
    public ResponseEntity<ErrorResponse> handleUnitAlreadyBookedException(
            UnitAlreadyBookedException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    @ExceptionHandler(EntityAlreadyExistsExcpetion.class)
    public ResponseEntity<ErrorResponse> handleEntityAlreadyExistsExcpetion(
            EntityAlreadyExistsExcpetion ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRangeExcpetion(
            InvalidDateRangeException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);

    }

    @ExceptionHandler(OverlappingBookingExcpetion.class)
    public ResponseEntity<ErrorResponse> handleOverlappingBookingExcpetion(
            OverlappingBookingExcpetion ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidPriceValueException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPriceValueException(
            InvalidPriceValueException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<ErrorResponse> handleS3UploadException(
            S3UploadException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileTypeException(
            InvalidFileTypeException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookingStateException(
            InvalidBookingStateException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ){
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
