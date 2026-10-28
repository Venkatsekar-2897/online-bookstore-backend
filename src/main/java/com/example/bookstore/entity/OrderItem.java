package com.example.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;
    @Column(nullable = false)
    private String bookTitle;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public OrderItem() {
    }

    public OrderItem(Book b, int q, BigDecimal price) {
        book = b;
        bookTitle = b.getTitle();
        quantity = q;
        unitPrice = price;
    }

    public void setOrder(CustomerOrder o) {
        order = o;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
