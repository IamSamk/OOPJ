package com.ticketing.pricing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public class PricingProperties {

    private String eventServiceBaseUrl = "http://localhost:8082";

    public String getEventServiceBaseUrl() {
        return eventServiceBaseUrl;
    }

    public void setEventServiceBaseUrl(String eventServiceBaseUrl) {
        this.eventServiceBaseUrl = eventServiceBaseUrl;
    }
}
