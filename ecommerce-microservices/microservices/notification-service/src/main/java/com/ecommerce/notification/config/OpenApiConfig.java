package com.ecommerce.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration for the Notification Service.
 * This service is Kafka-driven (no public REST write endpoints), but exposes
 * health and status endpoints documented here for observability.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Notification Service API")
                .description(
                    "Event-driven email notification service. Consumes Kafka events " +
                    "(order.created, payment.completed, payment.failed, customer.registered) " +
                    "and dispatches transactional emails via Spring Mail (SMTP). " +
                    "No inbound write endpoints — all processing is triggered by Kafka messages.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Enterprise E-Commerce Platform")
                    .email("admin@ecommerce.local")))
            .servers(List.of(
                new Server().url("http://localhost:8084").description("Direct (Dev)"),
                new Server().url("http://localhost:8080").description("Via API Gateway")));
    }
}
