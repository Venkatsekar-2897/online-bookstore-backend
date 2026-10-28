package com.example.bookstore.controller;

import com.example.bookstore.dto.PaymentDtos.PaymentRequest;
import com.example.bookstore.dto.PaymentDtos.PaymentResponse;
import com.example.bookstore.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/orders/{orderId}")
@Tag(name = "Payments", description = "Initiate and inspect demo payment attempts for the authenticated customer's orders.")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Initiate payment",
            description = "Creates one demo payment attempt for an order. COD remains pending; UPI and card methods are simulated as successful. No real payment is processed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment attempt created", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment method"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "A payment has already been initiated for this order")
    })
    public PaymentResponse initiate(
            Authentication authentication,
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request
    ) {
        return payments.initiate(authentication.getName(), orderId, request);
    }

    @GetMapping
    @Operation(summary = "Get payment status", description = "Returns the payment status for an order owned by the authenticated customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Order or payment not found")
    })
    public PaymentResponse status(Authentication authentication, @PathVariable Long orderId) {
        return payments.findForOrder(authentication.getName(), orderId);
    }
}
