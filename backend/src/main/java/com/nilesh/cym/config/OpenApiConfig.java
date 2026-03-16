package com.nilesh.cym.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI cymOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Call Your Mechanic API")
                        .version("v1")
                        .description("REST API for Call Your Mechanic. Auth and service catalog GET endpoints are public. Booking and session management endpoints require a Bearer JWT.")
                        .contact(new Contact().name("CYM Backend")))
                .components(new Components().addSecuritySchemes(
                        BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the access token issued by OTP verification or refresh.")
                ))
                .tags(List.of(
                        new Tag().name("Authentication").description("Public OTP-based authentication and role selection endpoints."),
                        new Tag().name("Sessions").description("Token lifecycle endpoints. Refresh is public, logout requires a valid authenticated session."),
                        new Tag().name("Services").description("Public service catalog lookup endpoints."),
                        new Tag().name("Bookings").description("Protected booking management endpoints for users and mechanics.")
                ));
    }
}
