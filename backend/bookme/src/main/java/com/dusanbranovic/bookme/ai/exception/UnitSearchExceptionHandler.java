package com.dusanbranovic.bookme.ai.exception;

import com.dusanbranovic.bookme.ai.controller.UnitSearchController;

import com.google.genai.errors.ApiException;
import com.dusanbranovic.bookme.exceptions.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.ai.model.tool.ToolCallLimitExceededException;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Instant;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = UnitSearchController.class)
public class UnitSearchExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(UnitSearchExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> validation(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "AI_INVALID_REQUEST", "Provide a nonblank message (maximum 2000 characters) and valid optional search context.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> failure(Exception ex, HttpServletRequest request) {
        boolean providerFailure = false;
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof UnitSearchException ai) return error(ai.status(), ai.code(), ai.getMessage(), request);
            if (cause instanceof ToolCallLimitExceededException) return error(HttpStatus.UNPROCESSABLE_ENTITY,
                    "AI_TOOL_LIMIT", "Too many tool calls. Narrow your request.", request);
            providerFailure |= cause instanceof NonTransientAiException || cause instanceof TransientAiException;
            int status = cause instanceof ApiException api ? api.code()
                    : cause instanceof RestClientResponseException http ? http.getStatusCode().value() : 0;
            if (status == 429) return error(HttpStatus.TOO_MANY_REQUESTS, "AI_PROVIDER_RATE_LIMIT", "AI provider quota or rate limit reached. Try later or check your quota.", request);
            if (cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException) {
                return error(HttpStatus.GATEWAY_TIMEOUT, "AI_PROVIDER_TIMEOUT", "AI provider timed out. Try again later.", request);
            }
            if (status != 0) return error(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_FAILURE", "AI provider request failed. Check provider configuration or try later.", request);
        }
        if (providerFailure) return error(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_FAILURE", "AI provider request failed. Check provider configuration or try later.", request);
        log.warn("AI request failed type={}", ex.getClass().getSimpleName());
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "AI_REQUEST_FAILED", "Smart filters could not complete this request.", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        var response = ResponseEntity.status(status);
        if (status == HttpStatus.TOO_MANY_REQUESTS) response.header("Retry-After", "60");
        return response.body(new ErrorResponse(code, message, status.value(), Instant.now(), request.getRequestURI()));
    }
}
