package com.ticketing.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BookingRequest(@NotNull UUID eventId,
                             @NotEmpty List<UUID> seatIds) {
}
