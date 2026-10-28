package com.example.bookstore.repository;

import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.CustomerOrder;
import com.example.bookstore.entity.Payment;
import com.example.bookstore.entity.PaymentMethod;
import com.example.bookstore.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {
    @Autowired
    private UserRepository users;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private PaymentRepository payments;

    @Test
    void persistsPaymentAndLoadsOrderByOwner() {
        AppUser user = users.save(new AppUser("reader@example.com", "password-hash", "Reader"));
        CustomerOrder order = orders.save(new CustomerOrder(user, new BigDecimal("50.00")));
        Payment payment = payments.save(new Payment(
                order,
                PaymentMethod.UPI,
                PaymentStatus.SUCCEEDED,
                order.getTotal()
        ));

        assertThat(orders.findByIdAndUserEmail(order.getId(), user.getEmail()))
                .isPresent();
        assertThat(payments.findByOrderId(order.getId()))
                .isPresent()
                .get()
                .extracting(Payment::getId)
                .isEqualTo(payment.getId());
        assertThat(payments.existsByOrderId(order.getId())).isTrue();
    }
}
