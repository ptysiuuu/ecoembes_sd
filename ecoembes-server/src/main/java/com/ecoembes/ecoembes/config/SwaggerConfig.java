package com.ecoembes.ecoembes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Configures the general OpenAPI definition for the Ecoembes API.
     * This includes title, version, description, and license information.
     */
    @Bean
    public OpenAPI ecoembesOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Ecoembes Central Server API - Prototype TW1 (Java)")
                        .description("API documentation for the Ecoembes project, prototype 1. This version uses simulated data and in-memory session management.")
                        .version("v1.0.0")
                        .license(new License().name("Deusto License").url("https://www.deusto.es"))
                );
    }
}