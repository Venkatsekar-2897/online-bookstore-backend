package com.example.bookstore.service.impl;

import com.example.bookstore.dto.BookDtos.*;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.BookService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class BookServiceImpl implements BookService {
    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);
    private final BookRepository books;

    public BookServiceImpl(BookRepository books) {
        this.books = books;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> findAll(Pageable pageable) {
        Page<BookResponse> result = books.findAll(pageable).map(this::toResponse);
        log.debug("Retrieved {} books from page {}", result.getNumberOfElements(), result.getNumber());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        log.debug("Looking up book id={}", id);
        return toResponse(findBook(id));
    }

    @Override
    @Transactional
    public BookResponse create(BookRequest r) {
        Book saved = books.save(new Book(r.title().trim(), r.author().trim(), r.price(), r.stock(), r.description()));
        log.info("Created book id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookRequest r) {
        Book b = findBook(id);
        b.setTitle(r.title().trim());
        b.setAuthor(r.author().trim());
        b.setPrice(r.price());
        b.setStock(r.stock());
        b.setDescription(r.description());
        log.info("Updated book id={}", id);
        return toResponse(b);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!books.existsById(id)) {
            log.warn("Cannot delete missing book id={}", id);
            throw new NoSuchElementException("Book not found: " + id);
        }
        books.deleteById(id);
        log.info("Deleted book id={}", id);
    }

    private Book findBook(Long id) {
        return books.findById(id).orElseThrow(() -> {
            log.warn("Book not found id={}", id);
            return new NoSuchElementException("Book not found: " + id);
        });
    }

    private BookResponse toResponse(Book b) {
        return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getPrice(), b.getStock(), b.getDescription());
    }
}
