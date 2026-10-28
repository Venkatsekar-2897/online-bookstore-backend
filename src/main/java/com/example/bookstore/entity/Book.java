package com.example.bookstore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "books", indexes = {@Index(name = "idx_books_title", columnList = "title"), @Index(name = "idx_books_author", columnList = "author")})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String author;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private int stock;
    @Version
    @Column(nullable = false)
    private long version;
    private String description;

    public Book() {
    }

    public Book(String title, String author, BigDecimal price, int stock, String description) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String v) {
        title = v;
    }

    public void setAuthor(String v) {
        author = v;
    }

    public void setPrice(BigDecimal v) {
        price = v;
    }

    public void setStock(int v) {
        stock = v;
    }

    public void setDescription(String v) {
        description = v;
    }
}
