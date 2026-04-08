package com.ticketing.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.ticketing")
@EntityScan(basePackages = "com.ticketing.pricing")
@EnableJpaRepositories(basePackages = "com.ticketing.pricing")
@ConfigurationPropertiesScan(basePackages = "com.ticketing.pricing")
public class PricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingApplication.class, args);
    }
}
