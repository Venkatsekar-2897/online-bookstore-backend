package com.example.bookstore.config;

import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner seedBooks(BookRepository books) {
        return args -> {
            if (books.count() == 0) {
                books.save(new Book("Clean Code", "Robert C. Martin", new BigDecimal("35.50"), 10, "A handbook of agile software craftsmanship"));
                books.save(new Book("Effective Java", "Joshua Bloch", new BigDecimal("42.00"), 8, "Best practices for the Java platform"));
                books.save(new Book("Design Patterns", "Erich Gamma et al.", new BigDecimal("49.99"), 5, "Reusable object-oriented design patterns"));
                log.info("Seeded initial book catalogue with 3 books");
            } else {
                log.debug("Skipping book seed because catalogue already contains data");
            }
        };
    }
}
