package com.example.bookstore.service.payment;

import com.example.bookstore.entity.PaymentMethod;
import com.example.bookstore.entity.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentMethod method();

    PaymentStatus initiate(BigDecimal amount);
}
