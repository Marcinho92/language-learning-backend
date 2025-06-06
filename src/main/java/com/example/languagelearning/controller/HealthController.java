package com.example.languagelearning.controller;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final ApplicationEventPublisher eventPublisher;

    public HealthController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        // Mark application as ACCEPTING_TRAFFIC on startup
        AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
    }

    @GetMapping("/actuator/health/liveness")
    public ResponseEntity<String> liveness() {
        return ResponseEntity.ok("UP");
    }

    @GetMapping("/actuator/health/readiness")
    public ResponseEntity<String> readiness() {
        return ResponseEntity.ok("UP");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Application is running");
    }
} 