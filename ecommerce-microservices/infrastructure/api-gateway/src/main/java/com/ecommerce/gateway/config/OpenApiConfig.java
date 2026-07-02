package com.ecommerce.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration for the API Gateway.
 *
 * <p>The gateway aggregates all downstream service API specs via the
 * {@code springdoc.swagger-ui.urls} list in {@code application.yml}.
 * This bean provides the top-level metadata shown in the Swagger UI header.
 *
 * <p>Access the unified Swagger UI at:
 * <pre>http://localhost:8080/swagger-ui.html</pre>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Enterprise E-Commerce API Gateway")
                .description(
                    "Unified API documentation for the entire e-commerce microservices platform. " +
                    "Use the dropdown at the top right to switch between service specs:\n\n" +
                    "- **Customer Service** (port 8081) — registration, JWT login, profiles\n" +
                    "- **Product Service** (port 8082) — catalog, categories, inventory\n" +
                    "- **Order Service** (port 8083) — cart, checkout, order lifecycle\n" +
                    "- **Notification Service** (port 8084) — Kafka-driven email events\n" +
                    "- **Admin Service** (port 8085) — dashboard, reports, Excel export\n" +
                    "- **Payment Service** (port 8086) — simulated payment processing\n\n" +
                    "All protected endpoints require a **Bearer JWT** obtained from " +
                    "`POST /api/customers/login`.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Enterprise E-Commerce Platform")
                    .email("admin@ecommerce.local")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("API Gateway (all services)")));
    }
}
