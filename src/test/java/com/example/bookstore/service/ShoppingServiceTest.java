package com.example.bookstore.service;

import com.example.bookstore.dto.CartDtos.AddCartRequest;
import com.example.bookstore.dto.CartDtos.CartResponse;
import com.example.bookstore.dto.CartDtos.OrderResponse;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.CartItem;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.impl.ShoppingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ShoppingServiceTest {
    @Mock private UserRepository users;
    @Mock private BookRepository books;
    @Mock private CartRepository carts;
    @Mock private OrderRepository orders;
    @InjectMocks private ShoppingService service;

    private AppUser user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new AppUser("reader@example.com", "hashed-password", "Reader");
        book = new Book("Clean Code", "Robert C. Martin", new BigDecimal("35.50"), 10, "Craftsmanship");
        book.setId(7L);
        lenient().when(users.findByEmail("reader@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void checkoutRejectsEmptyCartWithoutCreatingOrder() {
        when(carts.findByUserId(isNull())).thenReturn(List.of());

        assertThatThrownBy(() -> service.checkout("reader@example.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cart is empty");

        verifyNoInteractions(orders);
    }

    @Test
    void cartCalculatesLineAndCartTotals() {
        CartItem item = new CartItem(user, book, 2);
        when(carts.findByUserId(isNull())).thenReturn(List.of(item));

        CartResponse response = service.cart("reader@example.com");

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).bookId()).isEqualTo(7L);
        assertThat(response.items().get(0).lineTotal()).isEqualByComparingTo("71.00");
        assertThat(response.total()).isEqualByComparingTo("71.00");
    }

    @Test
    void addRejectsQuantityAboveAvailableStock() {
        when(books.findById(7L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> service.add("reader@example.com", new AddCartRequest(7L, 11)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Requested quantity exceeds available stock");

        verify(carts, never()).save(any(CartItem.class));
    }

    @Test
    void addSavesNewCartItemAndReturnsCart() {
        when(books.findById(7L)).thenReturn(Optional.of(book));
        when(carts.findByUserIdAndBookId(null, 7L)).thenReturn(Optional.empty());
        when(carts.findByUserId(isNull())).thenReturn(List.of(new CartItem(user, book, 2)));

        CartResponse response = service.add("reader@example.com", new AddCartRequest(7L, 2));

        verify(carts).save(argThat(item -> item.getBook().getId().equals(7L) && item.getQuantity() == 2));
        assertThat(response.total()).isEqualByComparingTo("71.00");
    }

    @Test
    void checkoutRejectsCartItemsWhenStockIsInsufficient() {
        book.setStock(1);
        CartItem item = new CartItem(user, book, 2);
        when(carts.findByUserId(isNull())).thenReturn(List.of(item));

        assertThatThrownBy(() -> service.checkout("reader@example.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Insufficient stock");

        verify(orders, never()).save(any());
        verify(carts, never()).deleteByUserId(isNull());
    }

    @Test
    void checkoutCreatesOrderReducesStockAndClearsCart() {
        CartItem item = new CartItem(user, book, 2);
        when(carts.findByUserId(isNull())).thenReturn(List.of(item));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.checkout("reader@example.com");

        assertThat(response.status()).isEqualTo("PLACED");
        assertThat(response.total()).isEqualByComparingTo("71.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(book.getStock()).isEqualTo(8);
        verify(orders).save(any());
        verify(carts).deleteByUserId(null);
    }

    @Test
    void cartFailsWhenUserDoesNotExist() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cart("missing@example.com"))
            .isInstanceOf(java.util.NoSuchElementException.class)
            .hasMessage("User not found");

        verifyNoInteractions(carts);
    }
}
