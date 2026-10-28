package com.example.bookstore.service;

import com.example.bookstore.dto.CartDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartOrderService {
    CartResponse add(String email, AddCartRequest request);

    CartResponse cart(String email);

    CartResponse update(String email, Long bookId, int quantity);

    void remove(String email, Long bookId);

    void clear(String email);

    OrderResponse checkout(String email);

    Page<OrderResponse> history(String email, Pageable pageable);

    OrderResponse order(String email, Long orderId);
}
