package com.example.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "customer_orders", indexes = {@Index(name = "idx_orders_user_created", columnList = "user_id,created_at"), @Index(name = "idx_orders_user_id", columnList = "user_id,id")})
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public CustomerOrder() {
    }

    public CustomerOrder(AppUser u, BigDecimal total) {
        user = u;
        this.total = total;
    }

    public void addItem(OrderItem i) {
        items.add(i);
        i.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
