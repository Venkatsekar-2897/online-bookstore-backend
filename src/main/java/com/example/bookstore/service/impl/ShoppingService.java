package com.example.bookstore.service.impl;

import com.example.bookstore.dto.CartDtos.AddCartRequest;
import com.example.bookstore.dto.CartDtos.CartLine;
import com.example.bookstore.dto.CartDtos.CartResponse;
import com.example.bookstore.dto.CartDtos.OrderLine;
import com.example.bookstore.dto.CartDtos.OrderResponse;
import com.example.bookstore.entity.AppUser;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.CartItem;
import com.example.bookstore.entity.CustomerOrder;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CartRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.CartOrderService;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ShoppingService implements CartOrderService {
    private static final Logger log = LoggerFactory.getLogger(ShoppingService.class);

    private final UserRepository users;
    private final BookRepository books;
    private final CartRepository carts;
    private final OrderRepository orders;

    public ShoppingService(UserRepository users, BookRepository books,
                           CartRepository carts, OrderRepository orders) {
        this.users = users;
        this.books = books;
        this.carts = carts;
        this.orders = orders;
    }

    @Override
    @Transactional
    public CartResponse add(String email, AddCartRequest request) {
        log.info("Adding book id={} to customer cart", request.bookId());
        AppUser user = findUser(email);
        Book book = books.findById(request.bookId())
                .orElseThrow(() -> new NoSuchElementException("Book not found: " + request.bookId()));

        CartItem existingItem = carts.findByUserIdAndBookId(user.getId(), book.getId()).orElse(null);
        int requestedQuantity = request.quantity() + (existingItem == null ? 0 : existingItem.getQuantity());
        if (requestedQuantity > book.getStock()) {
            log.warn("Cart add rejected: requested quantity exceeds stock for book id={}", book.getId());
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        if (existingItem == null) {
            carts.save(new CartItem(user, book, requestedQuantity));
        } else {
            existingItem.setQuantity(requestedQuantity);
        }
        return cart(email);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse cart(String email) {
        List<CartLine> lines = carts.findByUserId(findUser(email).getId()).stream()
                .map(this::toCartLine)
                .toList();
        BigDecimal total = lines.stream()
                .map(CartLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(lines, total);
    }

    @Override
    @Transactional
    public CartResponse update(String email, Long bookId, int quantity) {
        log.info("Updating cart quantity for book id={}", bookId);
        AppUser user = findUser(email);
        CartItem item = carts.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new NoSuchElementException("Cart item not found for book: " + bookId));
        if (quantity < 1 || quantity > item.getBook().getStock()) {
            log.warn("Cart quantity update rejected for book id={}", bookId);
            throw new IllegalArgumentException("Quantity must be between 1 and available stock");
        }
        item.setQuantity(quantity);
        return cart(email);
    }

    @Override
    @Transactional
    public void remove(String email, Long bookId) {
        carts.deleteByUserIdAndBookId(findUser(email).getId(), bookId);
        log.info("Removed book id={} from customer cart", bookId);
    }

    @Override
    @Transactional
    public void clear(String email) {
        carts.deleteByUserId(findUser(email).getId());
        log.info("Cleared customer cart");
    }

    @Override
    @Transactional
    public OrderResponse checkout(String email) {
        log.info("Starting checkout for authenticated customer");
        AppUser user = findUser(email);
        List<CartItem> items = carts.findByUserId(user.getId());
        if (items.isEmpty()) {
            log.warn("Checkout rejected because cart is empty");
            throw new IllegalArgumentException("Cart is empty");
        }

        for (CartItem item : items) {
            if (item.getBook().getStock() < item.getQuantity()) {
                log.warn("Checkout rejected due to insufficient stock for book id={}", item.getBook().getId());
                throw new IllegalArgumentException("Insufficient stock for: " + item.getBook().getTitle());
            }
        }

        BigDecimal total = items.stream()
                .map(item -> lineTotal(item.getBook().getPrice(), item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CustomerOrder order = new CustomerOrder(user, total);
        for (CartItem item : items) {
            Book book = item.getBook();
            order.addItem(new OrderItem(book, item.getQuantity(), book.getPrice()));
            // @Version on Book protects against concurrent checkouts overselling the same stock.
            book.setStock(book.getStock() - item.getQuantity());
        }

        CustomerOrder savedOrder = orders.save(order);
        carts.deleteByUserId(user.getId());
        log.info("Checkout completed; order id={}", savedOrder.getId());
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> history(String email, Pageable pageable) {
        log.debug("Retrieving customer order history, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return orders.findByUserIdOrderByCreatedAtDesc(findUser(email).getId(), pageable)
                .map(this::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse order(String email, Long orderId) {
        log.debug("Retrieving order id={} for authenticated customer", orderId);
        return toOrderResponse(orders.findByIdAndUserId(orderId, findUser(email).getId())
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId)));
    }

    private AppUser findUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Customer account was not found for authenticated principal");
                    return new NoSuchElementException("User not found");
                });
    }

    private CartLine toCartLine(CartItem item) {
        Book book = item.getBook();
        return new CartLine(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice(),
                item.getQuantity(), lineTotal(book.getPrice(), item.getQuantity()));
    }

    private OrderResponse toOrderResponse(CustomerOrder order) {
        List<OrderLine> lines = order.getItems().stream()
                .map(item -> new OrderLine(item.getBook().getId(), item.getBookTitle(), item.getQuantity(),
                        item.getUnitPrice(), lineTotal(item.getUnitPrice(), item.getQuantity())))
                .toList();
        return new OrderResponse(order.getId(), "PLACED", order.getCreatedAt(), order.getTotal(), lines);
    }

    private BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
