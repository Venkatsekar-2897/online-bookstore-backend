package com.example.bookstore.service.impl;

import com.example.bookstore.dto.PaymentDtos.PaymentRequest;
import com.example.bookstore.dto.PaymentDtos.PaymentResponse;
import com.example.bookstore.entity.CustomerOrder;
import com.example.bookstore.entity.Payment;
import com.example.bookstore.entity.PaymentStatus;
import com.example.bookstore.exception.PaymentConflictException;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.PaymentRepository;
import com.example.bookstore.service.PaymentService;
import com.example.bookstore.service.payment.PaymentStrategy;
import com.example.bookstore.service.payment.PaymentStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final PaymentStrategyFactory strategyFactory;

    public PaymentServiceImpl(
            OrderRepository orders,
            PaymentRepository payments,
            PaymentStrategyFactory strategyFactory
    ) {
        this.orders = orders;
        this.payments = payments;
        this.strategyFactory = strategyFactory;
    }

    @Override
    @Transactional
    public PaymentResponse initiate(String email, Long orderId, PaymentRequest request) {
        CustomerOrder order = findOrder(email, orderId);
        if (payments.existsByOrderId(orderId)) {
            throw new PaymentConflictException("A payment has already been initiated for this order");
        }

        PaymentStrategy strategy = strategyFactory.getStrategy(request.method());
        PaymentStatus status = strategy.initiate(order.getTotal());
        Payment payment = payments.save(new Payment(order, request.method(), status, order.getTotal()));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findForOrder(String email, Long orderId) {
        findOrder(email, orderId);
        Payment payment = payments.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found for order: " + orderId));
        return toResponse(payment);
    }

    private CustomerOrder findOrder(String email, Long orderId) {
        return orders.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCreatedAt()
        );
    }
}
