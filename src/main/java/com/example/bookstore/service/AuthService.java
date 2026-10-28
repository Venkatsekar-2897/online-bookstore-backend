package com.example.bookstore.service;

import com.example.bookstore.dto.AuthDtos.AuthResponse;
import com.example.bookstore.dto.AuthDtos.LoginRequest;
import com.example.bookstore.dto.AuthDtos.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
