package com.example.bookstore.exception;

public class PaymentConflictException extends RuntimeException {
    public PaymentConflictException(String message) {
        super(message);
    }
}
