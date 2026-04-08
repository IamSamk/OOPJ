package com.ticketing.common.exception;

import com.ticketing.common.api.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception, ServerWebExchange exchange) {
        return build(HttpStatus.NOT_FOUND, exception, exchange);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception, ServerWebExchange exchange) {
        return build(HttpStatus.CONFLICT, exception, exchange);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException exception, ServerWebExchange exchange) {
        return build(HttpStatus.BAD_REQUEST, exception, exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception exception, ServerWebExchange exchange) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, exception, exchange);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, Exception exception, ServerWebExchange exchange) {
        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
        ApiErrorResponse body = new ApiErrorResponse(
                status.getReasonPhrase(),
                exception.getMessage(),
                exchange.getRequest().getPath().value(),
                Instant.now(),
                correlationId
        );
        return ResponseEntity.status(status).body(body);
    }
}
