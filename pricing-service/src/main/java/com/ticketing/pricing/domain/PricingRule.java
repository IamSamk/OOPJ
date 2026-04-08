package com.ticketing.pricing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String seatType;

    @Column(nullable = false)
    private BigDecimal minMultiplier = BigDecimal.ONE;

    @Column(nullable = false)
    private BigDecimal maxMultiplier = BigDecimal.valueOf(3.0d);

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private long version;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public BigDecimal getMinMultiplier() {
        return minMultiplier;
    }

    public void setMinMultiplier(BigDecimal minMultiplier) {
        this.minMultiplier = minMultiplier;
    }

    public BigDecimal getMaxMultiplier() {
        return maxMultiplier;
    }

    public void setMaxMultiplier(BigDecimal maxMultiplier) {
        this.maxMultiplier = maxMultiplier;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getVersion() {
        return version;
    }
}
