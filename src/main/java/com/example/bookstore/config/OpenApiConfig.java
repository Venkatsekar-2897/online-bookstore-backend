package com.example.bookstore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Bookstore Backend API")
                .version("1.0.0")
                .description("REST API for customer authentication, book catalogue management, shopping carts, and orders. Public catalogue reads and authentication endpoints do not require a token; cart and order endpoints require a JWT. Book management requires ADMIN authority.")
                .contact(new Contact().name("Bookstore Development Team").email("support@example.com")))
            .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                    .description("Enter the JWT access token returned by /api/auth/login. Swagger UI adds the Bearer prefix automatically.")));
    }
}
