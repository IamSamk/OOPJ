package com.ticketing.common.api;

public record ApiResponse<T>(boolean success, String message, T data, String correlationId) {

    public static <T> ApiResponse<T> success(String message, T data, String correlationId) {
        return new ApiResponse<>(true, message, data, correlationId);
    }
}
