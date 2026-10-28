package com.example.bookstore.controller;

import com.example.bookstore.dto.CartDtos.*;
import com.example.bookstore.service.CartOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "Manage the authenticated customer's shopping cart.")
@SecurityRequirement(name = "bearerAuth")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartOrderService service;

    public CartController(CartOrderService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "View cart", description = "Returns the current customer's cart items and calculated total.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cart returned", content = @Content(schema = @Schema(implementation = CartResponse.class))), @ApiResponse(responseCode = "401", description = "Authentication required")})
    public CartResponse view(Authentication authentication) {
        log.debug("Viewing cart for authenticated customer");
        return service.cart(authentication.getName());
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a book and quantity to the current customer's cart. The requested total quantity cannot exceed available stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item added", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(schema = @Schema(implementation = com.example.bookstore.dto.ApiError.class)))
    })
    public ResponseEntity<CartResponse> add(Authentication authentication, @Valid @RequestBody AddCartRequest request) {
        log.info("Adding book id={} to cart", request.bookId());
        return ResponseEntity.status(201).body(service.add(authentication.getName(), request));
    }

    @PutMapping("/items/{bookId}")
    @Operation(summary = "Update cart item quantity", description = "Sets the quantity of a book already in the customer's cart. Quantity must be at least one and cannot exceed stock.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cart item updated", content = @Content(schema = @Schema(implementation = CartResponse.class))), @ApiResponse(responseCode = "400", description = "Invalid quantity"), @ApiResponse(responseCode = "404", description = "Cart item not found"), @ApiResponse(responseCode = "401", description = "Authentication required")})
    public CartResponse update(Authentication authentication, @Parameter(description = "Book identifier", example = "1", required = true) @PathVariable Long bookId, @Parameter(description = "New quantity", example = "2", required = true) @RequestParam int quantity) {
        log.info("Updating cart quantity for book id={}", bookId);
        return service.update(authentication.getName(), bookId, quantity);
    }

    @DeleteMapping("/items/{bookId}")
    @Operation(summary = "Remove item from cart", description = "Removes the specified book from the customer's cart.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Item removed"), @ApiResponse(responseCode = "401", description = "Authentication required")})
    public ResponseEntity<Void> remove(Authentication authentication, @Parameter(description = "Book identifier", example = "1", required = true) @PathVariable Long bookId) {
        log.info("Removing book id={} from cart", bookId);
        service.remove(authentication.getName(), bookId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the authenticated customer's cart.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Cart cleared"), @ApiResponse(responseCode = "401", description = "Authentication required")})
    public ResponseEntity<Void> clear(Authentication authentication) {
        log.info("Clearing cart for authenticated customer");
        service.clear(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
