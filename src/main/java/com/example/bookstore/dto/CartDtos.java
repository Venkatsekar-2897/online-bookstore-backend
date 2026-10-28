package com.example.bookstore.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class CartDtos {
    public record AddCartRequest(@NotNull Long bookId, @Min(1) @Max(99) int quantity) {
    }

    public record CartLine(Long bookId, String title, String author, BigDecimal unitPrice, int quantity,
                           BigDecimal lineTotal) {
    }

    public record CartResponse(List<CartLine> items, BigDecimal total) {
    }

    public record OrderLine(Long bookId, String title, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public record OrderResponse(Long orderId, String status, java.time.Instant createdAt, BigDecimal total,
                                List<OrderLine> items) {
    }
}
