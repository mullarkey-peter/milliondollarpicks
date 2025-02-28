package com.glizzy.milliondollarpicks.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserNotFoundException(UserNotFoundException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, HttpStatus.NOT_FOUND, exchange);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateUserException(DuplicateUserException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, HttpStatus.CONFLICT, exchange);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidCredentialsException(InvalidCredentialsException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, HttpStatus.UNAUTHORIZED, exchange);
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleJwtAuthenticationException(JwtAuthenticationException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, HttpStatus.UNAUTHORIZED, exchange);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDeniedException(AccessDeniedException ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, HttpStatus.FORBIDDEN, exchange);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGlobalException(Exception ex, ServerWebExchange exchange) {
        return createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, exchange);
    }

    private Mono<ResponseEntity<ErrorResponse>> createErrorResponse(Exception ex, HttpStatus status, ServerWebExchange exchange) {
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
            .status(status)
            .body(errorResponse));
    }
} 