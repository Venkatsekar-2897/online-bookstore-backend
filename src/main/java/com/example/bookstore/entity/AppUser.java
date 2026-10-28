package com.example.bookstore.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users", indexes = @Index(name = "idx_app_users_email", columnList = "email", unique = true))
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, length = 20)
    private String role = "USER";

    public AppUser() {
    }

    public AppUser(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
