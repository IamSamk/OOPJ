package com.ticketing.event.kafka;

import com.ticketing.common.kafka.SeatLockedEvent;
import com.ticketing.common.model.SeatStatus;
import com.ticketing.event.service.EventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SeatLockListener {

    private final EventService eventService;

    public SeatLockListener(EventService eventService) {
        this.eventService = eventService;
    }

    @KafkaListener(topics = "seat.locked", groupId = "event-service")
    public void handle(SeatLockedEvent event) {
        eventService.updateSeatStatuses(event.eventId(), event.seatIds(), SeatStatus.HELD).subscribe();
    }
}
