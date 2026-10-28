package com.example.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(name = "uk_cart_user_book", columnNames = {"user_id", "book_id"}), indexes = {@Index(name = "idx_cart_user", columnList = "user_id"), @Index(name = "idx_cart_book", columnList = "book_id")})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;
    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;
    @Column(nullable = false)
    private int quantity;

    public CartItem() {
    }

    public CartItem(AppUser u, Book b, int q) {
        user = u;
        book = b;
        quantity = q;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        quantity = q;
    }
}
