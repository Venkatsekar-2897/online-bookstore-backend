# Bookstore Backend

A Spring Boot 3 / Java 17 REST API for a bookstore assignment. The code uses service contracts, constructor injection, DTOs, validation, Spring Data JPA, transactional checkout, and standard RS256-signed JWTs.

## Requirements

- JDK 17+
- Maven 3.9+

## Run

```bash
mvn clean test
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. H2 uses a local file database under `./data/bookstore`.

## Main endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a USER |
| POST | `/api/auth/login` | Public | Authenticate and receive a JWT |
| GET | `/api/books?page=0&size=20&sort=title,asc` | Public | Paginated book catalogue |
| GET | `/api/books/{id}` | Public | Book details |
| POST | `/api/books` | ADMIN | Create a book |
| PUT | `/api/books/{id}` | ADMIN | Update a book |
| DELETE | `/api/books/{id}` | ADMIN | Delete a book |
| GET | `/api/cart` | Authenticated | View current user's cart |
| POST | `/api/cart/items` | Authenticated | Add an item |
| PUT | `/api/cart/items/{bookId}?quantity=2` | Authenticated | Update quantity |
| DELETE | `/api/cart/items/{bookId}` | Authenticated | Remove an item |
| DELETE | `/api/cart` | Authenticated | Clear cart |
| POST | `/api/orders/checkout` | Authenticated | Create order from cart |
| GET | `/api/orders?page=0&size=20` | Authenticated | Paginated order history |
| GET | `/api/orders/{id}` | Authenticated | Read one of the current user's orders |

After registration/login, send `Authorization: Bearer <token>` for authenticated endpoints.

### Example registration

```http
POST /api/auth/register
Content-Type: application/json

{
  "fullName": "Test User",
  "email": "test@example.com",
  "password": "password123"
}
```

### Example add-to-cart

```http
POST /api/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "bookId": 1,
  "quantity": 2
}
```

## Architecture and decisions

- Controllers only translate HTTP requests/responses; business rules live in services.
- Controllers depend on service interfaces (`AuthService`, `BookService`, `CartOrderService`).
- DTOs are Java records; request validation uses Jakarta Bean Validation.
- `BigDecimal` is used for monetary values.
- JPA repositories abstract persistence. `@EntityGraph` is used for cart/book reads to avoid a common N+1 access pattern.
- Book catalogue and order history use bounded pagination (configured maximum page size 100).
- Database indexes support book title/author lookup, user email lookup, cart ownership, and order history.
- `@Version` on `Book` enables optimistic locking. If two checkouts race to decrement the same stock, the losing transaction should fail and roll back rather than silently oversell inventory. Clients should treat HTTP 409 as a retryable conflict.
- Checkout is transactional and snapshots each order item's title, unit price, and quantity, so historical orders do not depend on future price/title changes.
- JWTs are signed using RS256 via Spring Security's `JwtEncoder`; the resource server validates signature, timestamps, and issuer. Passwords are BCrypt hashes.
- RSA keys are generated on startup for local development only. Restarting invalidates previously issued tokens. Production should load a persistent key pair from a secret manager/KMS and rotate keys deliberately.
- Public users can browse books. Book write endpoints require `SCOPE_ADMIN`; user tokens contain `scope=USER` by default.

### Local admin provisioning (development only)

Register a normal account, then use the H2 console at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/bookstore`, user `sa`, blank password) to promote that account:

```sql
UPDATE app_users SET role = 'ADMIN' WHERE email = 'test@example.com';
```

Log out and log in again to receive a token containing `scope=ADMIN`. Do not expose the H2 console or use manual database role changes as a production admin workflow.

## Tests

The test suite includes unit tests for book-service pagination/mapping, missing-book behavior, input normalization, and token creation. Run with `mvn clean test`.

## Production hardening still required

This is a learning/assignment project, not a fully production-ready commerce platform. Before production, add migration-managed schema (Flyway/Liquibase), persistent managed signing keys, refresh/revocation policy, rate limiting, audit logging, admin provisioning/authorization policy, integration tests against the chosen production database, observability, and a deliberate checkout concurrency/retry policy. If the app needs multi-instance operation, use a shared database and centralized key management.
