package com.example.bookstore.dto;

import com.example.bookstore.entity.PaymentMethod;
import com.example.bookstore.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    public record PaymentRequest(@NotNull PaymentMethod method) {
    }

    public record PaymentResponse(
            Long paymentId,
            Long orderId,
            PaymentMethod method,
            PaymentStatus status,
            BigDecimal amount,
            Instant createdAt
    ) {
    }
}
