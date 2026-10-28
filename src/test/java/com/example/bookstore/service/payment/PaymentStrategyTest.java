package com.example.bookstore.service.payment;

import com.example.bookstore.entity.PaymentMethod;
import com.example.bookstore.entity.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStrategyTest {
    private final CashOnDeliveryPaymentStrategy cashOnDelivery = new CashOnDeliveryPaymentStrategy();
    private final UpiPaymentStrategy upi = new UpiPaymentStrategy();
    private final CreditCardPaymentStrategy creditCard = new CreditCardPaymentStrategy();
    private final DebitCardPaymentStrategy debitCard = new DebitCardPaymentStrategy();

    @Test
    void strategiesReturnExpectedDemoStatuses() {
        BigDecimal amount = new BigDecimal("25.00");

        assertThat(cashOnDelivery.initiate(amount)).isEqualTo(PaymentStatus.PENDING);
        assertThat(upi.initiate(amount)).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(creditCard.initiate(amount)).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(debitCard.initiate(amount)).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    @Test
    void factoryProvidesStrategyForEveryPaymentMethod() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory(
                List.of(cashOnDelivery, upi, creditCard, debitCard)
        );

        assertThat(factory.getStrategy(PaymentMethod.CASH_ON_DELIVERY)).isSameAs(cashOnDelivery);
        assertThat(factory.getStrategy(PaymentMethod.UPI)).isSameAs(upi);
        assertThat(factory.getStrategy(PaymentMethod.CREDIT_CARD)).isSameAs(creditCard);
        assertThat(factory.getStrategy(PaymentMethod.DEBIT_CARD)).isSameAs(debitCard);
    }

    @Test
    void factoryRejectsMissingStrategies() {
        assertThatThrownBy(() -> new PaymentStrategyFactory(List.of(cashOnDelivery)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No payment strategy registered");
    }
}
