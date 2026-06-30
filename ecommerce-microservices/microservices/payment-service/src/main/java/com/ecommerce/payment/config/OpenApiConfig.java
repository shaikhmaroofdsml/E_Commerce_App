package com.ecommerce.payment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Payment Service API")
                .description("Simulated payment processing engine. Consumes order.created Kafka events, " +
                    "processes payment with 90% success rate, and publishes payment.completed or payment.failed.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Enterprise E-Commerce Platform")
                    .email("admin@ecommerce.local")))
            .servers(List.of(
                new Server().url("http://localhost:8086").description("Direct (Dev)"),
                new Server().url("http://localhost:8080").description("Via API Gateway")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
            .components(new Components()
                .addSecuritySchemes("Bearer Token", new SecurityScheme()
                    .name("Bearer Token")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
