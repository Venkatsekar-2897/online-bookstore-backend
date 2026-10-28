package com.example.bookstore.service;

import com.example.bookstore.dto.BookDtos.BookRequest;
import com.example.bookstore.dto.BookDtos.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Page<BookResponse> findAll(Pageable pageable);

    BookResponse findById(Long id);

    BookResponse create(BookRequest request);

    BookResponse update(Long id, BookRequest request);

    void delete(Long id);
}
