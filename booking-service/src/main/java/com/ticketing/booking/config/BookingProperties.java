package com.ticketing.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public class BookingProperties {

    private String eventServiceBaseUrl = "http://localhost:8082";
    private String pricingServiceBaseUrl = "http://localhost:8084";
    private String paymentServiceBaseUrl = "http://localhost:8085";
    private long seatHoldMinutes = 15;

    public String getEventServiceBaseUrl() {
        return eventServiceBaseUrl;
    }

    public void setEventServiceBaseUrl(String eventServiceBaseUrl) {
        this.eventServiceBaseUrl = eventServiceBaseUrl;
    }

    public String getPricingServiceBaseUrl() {
        return pricingServiceBaseUrl;
    }

    public void setPricingServiceBaseUrl(String pricingServiceBaseUrl) {
        this.pricingServiceBaseUrl = pricingServiceBaseUrl;
    }

    public String getPaymentServiceBaseUrl() {
        return paymentServiceBaseUrl;
    }

    public void setPaymentServiceBaseUrl(String paymentServiceBaseUrl) {
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
    }

    public long getSeatHoldMinutes() {
        return seatHoldMinutes;
    }

    public void setSeatHoldMinutes(long seatHoldMinutes) {
        this.seatHoldMinutes = seatHoldMinutes;
    }
}
