package com.ticketing.common.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SeatView(UUID id,
                       UUID eventId,
                       String seatNumber,
                       String seatType,
                       BigDecimal price,
                       String status,
                       long version) {
}
