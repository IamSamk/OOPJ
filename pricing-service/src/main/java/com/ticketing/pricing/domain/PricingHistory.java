package com.ticketing.pricing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pricing_history", indexes = {
        @Index(name = "idx_pricing_history_event_created", columnList = "eventId, createdAt")
})
public class PricingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String seatType;

    @Column(nullable = false)
    private BigDecimal calculatedPrice;

    @Column(nullable = false)
    private double demandMultiplier;

    @Column(nullable = false)
    private double timeFactor;

    @Column(nullable = false)
    private double scarcityFactor;

    @Column(nullable = false)
    private double bookingVelocity;

    @Column(nullable = false)
    private int remainingSeats;

    @Column(nullable = false)
    private int activeViews;

    @Column(nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public BigDecimal getCalculatedPrice() {
        return calculatedPrice;
    }

    public void setCalculatedPrice(BigDecimal calculatedPrice) {
        this.calculatedPrice = calculatedPrice;
    }

    public double getDemandMultiplier() {
        return demandMultiplier;
    }

    public void setDemandMultiplier(double demandMultiplier) {
        this.demandMultiplier = demandMultiplier;
    }

    public double getTimeFactor() {
        return timeFactor;
    }

    public void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }

    public double getScarcityFactor() {
        return scarcityFactor;
    }

    public void setScarcityFactor(double scarcityFactor) {
        this.scarcityFactor = scarcityFactor;
    }

    public double getBookingVelocity() {
        return bookingVelocity;
    }

    public void setBookingVelocity(double bookingVelocity) {
        this.bookingVelocity = bookingVelocity;
    }

    public int getRemainingSeats() {
        return remainingSeats;
    }

    public void setRemainingSeats(int remainingSeats) {
        this.remainingSeats = remainingSeats;
    }

    public int getActiveViews() {
        return activeViews;
    }

    public void setActiveViews(int activeViews) {
        this.activeViews = activeViews;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
