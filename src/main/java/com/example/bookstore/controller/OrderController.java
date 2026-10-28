package com.example.bookstore.controller;

import com.example.bookstore.dto.CartDtos.OrderResponse;
import com.example.bookstore.service.CartOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Checkout the shopping cart and view the authenticated customer's orders.")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final CartOrderService service;

    public OrderController(CartOrderService service) {
        this.service = service;
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart", description = "Creates an order from the customer's cart, verifies stock, reduces inventory, and clears the cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cart is empty or stock is insufficient", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "409", description = "Concurrent inventory update", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public ResponseEntity<OrderResponse> checkout(Authentication authentication) {
        log.info("Checkout requested by authenticated customer");
        return ResponseEntity.status(201).body(service.checkout(authentication.getName()));
    }

    @GetMapping
    @Operation(summary = "List order history", description = "Returns the authenticated customer's orders, newest first, using pagination.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Order history returned"), @ApiResponse(responseCode = "401", description = "Authentication required")})
    public Page<OrderResponse> history(Authentication authentication, @Parameter(description = "Pagination options; for example, page=0&size=20") @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Listing order history: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return service.history(authentication.getName(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details", description = "Returns an order owned by the authenticated customer. Customers cannot view another customer's orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public OrderResponse order(Authentication authentication, @Parameter(description = "Order identifier", example = "1", required = true) @PathVariable Long id) {
        log.debug("Fetching order id={} for authenticated customer", id);
        return service.order(authentication.getName(), id);
    }
}
