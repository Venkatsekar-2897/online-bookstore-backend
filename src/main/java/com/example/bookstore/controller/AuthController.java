package com.example.bookstore.controller;

import com.example.bookstore.dto.AuthDtos.*;
import com.example.bookstore.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register customers and authenticate to obtain a JWT access token.")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a customer", description = "Creates a customer account and returns a bearer JWT. Email addresses are normalized before registration.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already registered", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received");
        AuthResponse response = authService.register(request);
        log.info("Customer registration completed");
        return response;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Validates customer credentials and returns a JWT bearer token for authenticated API calls.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email or password", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received");
        AuthResponse response = authService.login(request);
        log.info("Login completed successfully");
        return response;
    }
}
