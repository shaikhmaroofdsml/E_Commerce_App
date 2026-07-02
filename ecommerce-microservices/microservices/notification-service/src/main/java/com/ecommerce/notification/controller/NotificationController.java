package com.ecommerce.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight status controller for the Notification Service.
 *
 * <p>This service is entirely event-driven: it does NOT expose write endpoints.
 * All email notifications are triggered by Kafka consumer events:
 * <ul>
 *   <li>{@code order.created}      → order confirmation email</li>
 *   <li>{@code payment.completed}  → payment success email</li>
 *   <li>{@code payment.failed}     → payment failure email</li>
 *   <li>{@code customer.registered} → welcome email</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(
    name = "Notification Service",
    description = "Event-driven email notification service. " +
        "Consumes Kafka topics (order.created, payment.completed, payment.failed, customer.registered) " +
        "and dispatches transactional emails. No inbound write endpoints — Kafka drives all processing."
)
public class NotificationController {

    /**
     * Health ping — useful for gateway circuit breaker checks and Swagger UI smoke testing.
     */
    @Operation(
        summary = "Notification service health ping",
        description = "Returns service status and a list of Kafka topics this service consumes."
    )
    @ApiResponse(responseCode = "200", description = "Service is running and consuming Kafka events")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "service",       "notification-service",
            "status",        "UP",
            "mode",          "event-driven (Kafka consumer only)",
            "kafkaTopics",   new String[]{
                "order.created",
                "payment.completed",
                "payment.failed",
                "customer.registered"
            }
        ));
    }
}
