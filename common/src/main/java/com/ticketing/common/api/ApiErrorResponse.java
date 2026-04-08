package com.ticketing.common.api;

import java.time.Instant;

public record ApiErrorResponse(String error, String message, String path, Instant timestamp, String correlationId) {
}
