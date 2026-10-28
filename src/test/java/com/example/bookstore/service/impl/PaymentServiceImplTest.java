package com.example.bookstore.service.impl;

import com.example.bookstore.dto.PaymentDtos.PaymentRequest;
import com.example.bookstore.dto.PaymentDtos.PaymentResponse;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.CustomerOrder;
import com.example.bookstore.entity.Payment;
import com.example.bookstore.entity.PaymentMethod;
import com.example.bookstore.entity.PaymentStatus;
import com.example.bookstore.exception.PaymentConflictException;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.PaymentRepository;
import com.example.bookstore.service.payment.PaymentStrategy;
import com.example.bookstore.service.payment.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {
    private final OrderRepository orders = mock(OrderRepository.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final PaymentStrategyFactory strategyFactory = mock(PaymentStrategyFactory.class);
    private final PaymentStrategy strategy = mock(PaymentStrategy.class);
    private final PaymentServiceImpl service = new PaymentServiceImpl(orders, payments, strategyFactory);

    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        order = new CustomerOrder(new AppUser("reader@example.com", "hash", "Reader"), new BigDecimal("50.00"));
        ReflectionTestUtils.setField(order, "id", 12L);
    }

    @Test
    void initiateSavesPaymentForOwnedOrderUsingSelectedStrategy() {
        when(orders.findByIdAndUserEmail(12L, "reader@example.com")).thenReturn(Optional.of(order));
        when(payments.existsByOrderId(12L)).thenReturn(false);
        when(strategyFactory.getStrategy(PaymentMethod.UPI)).thenReturn(strategy);
        when(strategy.initiate(order.getTotal())).thenReturn(PaymentStatus.SUCCEEDED);
        when(payments.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = service.initiate(
                "reader@example.com",
                12L,
                new PaymentRequest(PaymentMethod.UPI)
        );

        assertThat(response.orderId()).isEqualTo(12L);
        assertThat(response.method()).isEqualTo(PaymentMethod.UPI);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(response.amount()).isEqualByComparingTo("50.00");
        verify(payments).save(any(Payment.class));
    }

    @Test
    void initiateRejectsOrderThatDoesNotBelongToCustomer() {
        when(orders.findByIdAndUserEmail(12L, "other@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.initiate(
                "other@example.com",
                12L,
                new PaymentRequest(PaymentMethod.UPI)
        ))
                .isInstanceOf(java.util.NoSuchElementException.class);

        verifyNoInteractions(payments, strategyFactory);
    }

    @Test
    void initiateRejectsSecondPaymentForOrder() {
        when(orders.findByIdAndUserEmail(12L, "reader@example.com")).thenReturn(Optional.of(order));
        when(payments.existsByOrderId(12L)).thenReturn(true);

        assertThatThrownBy(() -> service.initiate(
                "reader@example.com",
                12L,
                new PaymentRequest(PaymentMethod.UPI)
        ))
                .isInstanceOf(PaymentConflictException.class)
                .hasMessageContaining("already been initiated");

        verify(payments, never()).save(any(Payment.class));
        verifyNoInteractions(strategyFactory);
    }

    @Test
    void findForOrderReturnsPaymentOnlyAfterConfirmingOwnership() {
        Payment payment = new Payment(
                order,
                PaymentMethod.CREDIT_CARD,
                PaymentStatus.SUCCEEDED,
                order.getTotal()
        );
        when(orders.findByIdAndUserEmail(12L, "reader@example.com")).thenReturn(Optional.of(order));
        when(payments.findByOrderId(12L)).thenReturn(Optional.of(payment));

        PaymentResponse response = service.findForOrder("reader@example.com", 12L);

        assertThat(response.orderId()).isEqualTo(12L);
        assertThat(response.method()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCEEDED);
    }
}
