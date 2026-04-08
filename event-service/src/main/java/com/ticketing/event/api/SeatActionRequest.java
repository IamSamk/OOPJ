package com.ticketing.event.api;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record SeatActionRequest(@NotEmpty List<UUID> seatIds) {
}
