package com.example.bookstore.service;

import com.example.bookstore.dto.BookDtos.BookRequest;
import com.example.bookstore.dto.BookDtos.BookResponse;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @Mock private BookRepository books;
    @InjectMocks private BookServiceImpl service;
    private Book cleanCode;

    @BeforeEach
    void setUp() {
        cleanCode = new Book("Clean Code", "Robert C. Martin", new BigDecimal("35.50"), 10, "Craftsmanship");
        cleanCode.setId(1L);
    }

    @Test
    void findAllReturnsMappedPageAndPreservesPaginationMetadata() {
        PageRequest pageable = PageRequest.of(1, 1);
        when(books.findAll(pageable)).thenReturn(new PageImpl<>(List.of(cleanCode), pageable, 2));

        Page<BookResponse> result = service.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Clean Code");
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(1);
        verify(books).findAll(pageable);
    }

    @Test
    void findByIdReturnsMappedBook() {
        when(books.findById(1L)).thenReturn(Optional.of(cleanCode));

        BookResponse result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.author()).isEqualTo("Robert C. Martin");
        assertThat(result.price()).isEqualByComparingTo("35.50");
        assertThat(result.stock()).isEqualTo(10);
    }

    @Test
    void findByIdThrowsWhenBookDoesNotExist() {
        when(books.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessageContaining("404");
    }

    @Test
    void createTrimsTitleAndAuthor() {
        BookRequest request = new BookRequest(" Clean Code ", " Robert C. Martin ",
            new BigDecimal("35.50"), 10, "Craftsmanship");
        when(books.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse result = service.create(request);

        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.author()).isEqualTo("Robert C. Martin");
        assertThat(result.price()).isEqualByComparingTo("35.50");
        verify(books).save(any(Book.class));
    }

    @Test
    void updateChangesBookFieldsAndTrimsText() {
        when(books.findById(1L)).thenReturn(Optional.of(cleanCode));
        BookRequest request = new BookRequest(" Effective Java ", " Joshua Bloch ",
            new BigDecimal("42.00"), 8, "Best practices");

        BookResponse result = service.update(1L, request);

        assertThat(result.title()).isEqualTo("Effective Java");
        assertThat(result.author()).isEqualTo("Joshua Bloch");
        assertThat(result.price()).isEqualByComparingTo("42.00");
        assertThat(result.stock()).isEqualTo(8);
        assertThat(result.description()).isEqualTo("Best practices");
        verify(books, never()).save(any(Book.class));
    }

    @Test
    void updateThrowsWhenBookDoesNotExist() {
        when(books.findById(404L)).thenReturn(Optional.empty());
        BookRequest request = new BookRequest("Title", "Author", new BigDecimal("10.00"), 1, null);

        assertThatThrownBy(() -> service.update(404L, request))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Book not found: 404");
    }

    @Test
    void deleteRemovesExistingBook() {
        when(books.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(books).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenBookDoesNotExist() {
        when(books.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(404L))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Book not found: 404");

        verify(books, never()).deleteById(anyLong());
    }
}
