package com.coopcredit.riskcentralmock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Risk Central Mock Service.
 * This is a lightweight microservice that simulates external risk evaluation.
 * No security, no database - just deterministic risk scoring based on document hash.
 */
@SpringBootApplication
public class RiskCentralMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskCentralMockApplication.class, args);
    }
}