package com.businesspilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BusinessPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessPilotApplication.class, args);
    }
}
