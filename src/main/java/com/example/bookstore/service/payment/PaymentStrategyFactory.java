package com.example.bookstore.service.payment;

import com.example.bookstore.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PaymentStrategyFactory {
    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        Map<PaymentMethod, PaymentStrategy> strategyMap = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy strategy : strategies) {
            PaymentStrategy previous = strategyMap.put(strategy.method(), strategy);
            if (previous != null) {
                throw new IllegalStateException("Multiple payment strategies registered for " + strategy.method());
            }
        }

        for (PaymentMethod method : PaymentMethod.values()) {
            if (!strategyMap.containsKey(method)) {
                throw new IllegalStateException("No payment strategy registered for " + method);
            }
        }
        this.strategies = Map.copyOf(strategyMap);
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        Objects.requireNonNull(method, "Payment method is required");
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
        return strategy;
    }
}
