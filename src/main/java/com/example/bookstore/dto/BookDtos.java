package com.example.bookstore.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class BookDtos {
    public record BookRequest(@NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 150) String author,
                              @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
                              @Min(0) @Max(100000) int stock, String description) {
    }

    public record BookResponse(Long id, String title, String author, BigDecimal price, int stock, String description) {
    }
}
