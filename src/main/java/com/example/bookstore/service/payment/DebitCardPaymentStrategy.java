package com.example.bookstore.service.payment;

import com.example.bookstore.entity.PaymentMethod;
import com.example.bookstore.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DebitCardPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentMethod method() {
        return PaymentMethod.DEBIT_CARD;
    }

    @Override
    public PaymentStatus initiate(BigDecimal amount) {
        return PaymentStatus.SUCCEEDED;
    }
}
