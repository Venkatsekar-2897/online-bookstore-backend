package com.example.bookstore.service.impl;

import com.example.bookstore.dto.AuthDtos.*;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.TokenService;
import com.example.bookstore.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthServiceImpl(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        log.debug("Registering customer account");
        if (users.existsByEmail(email)) {
            log.warn("Registration rejected because email is already registered");
            throw new IllegalArgumentException("Email is already registered");
        }
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password()), request.fullName().trim()));
        log.info("Customer account created");
        return response(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.debug("Authenticating customer credentials");
        AppUser user = users.findByEmail(normalize(request.email())).orElseThrow(() -> {
            log.warn("Login rejected: invalid credentials");
            return new IllegalArgumentException("Invalid email or password");
        });
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login rejected: invalid credentials");
            throw new IllegalArgumentException("Invalid email or password");
        }
        log.info("Customer authenticated successfully");
        return response(user);
    }

    private AuthResponse response(AppUser user) {
        return new AuthResponse(tokens.create(user.getEmail(), user.getRole()), "Bearer", user.getEmail(), user.getFullName());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
