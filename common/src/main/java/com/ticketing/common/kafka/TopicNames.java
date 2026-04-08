package com.ticketing.common.kafka;

public final class TopicNames {

    public static final String BOOKING_CREATED = "booking.created";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String SEAT_LOCKED = "seat.locked";
    public static final String PRICE_UPDATED = "price.updated";
    public static final String WEBHOOK_DLQ = "webhook.dlq";

    private TopicNames() {
    }
}
