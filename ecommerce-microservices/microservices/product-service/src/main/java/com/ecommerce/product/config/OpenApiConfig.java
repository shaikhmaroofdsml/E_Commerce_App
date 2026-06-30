package com.ecommerce.product.config;

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
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Product Service API")
                .description("Product catalog management — search, filtering, pagination, " +
                    "category CRUD, and inventory tracking.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Enterprise E-Commerce Platform")
                    .email("admin@ecommerce.local")))
            .servers(List.of(
                new Server().url("http://localhost:8082").description("Direct (Dev)"),
                new Server().url("http://localhost:8080").description("Via API Gateway")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
            .components(new Components()
                .addSecuritySchemes("Bearer Token", new SecurityScheme()
                    .name("Bearer Token")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Admin JWT required for write operations")));
    }
}
