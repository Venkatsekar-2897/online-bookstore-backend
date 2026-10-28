package com.example.bookstore.service;

import com.example.bookstore.dto.PaymentDtos.PaymentRequest;
import com.example.bookstore.dto.PaymentDtos.PaymentResponse;

public interface PaymentService {
    PaymentResponse initiate(String email, Long orderId, PaymentRequest request);

    PaymentResponse findForOrder(String email, Long orderId);
}
