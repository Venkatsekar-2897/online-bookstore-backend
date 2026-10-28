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

## Swagger UI and OpenAPI

With the backend running, open the interactive API documentation at:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

Swagger UI lets you inspect endpoint descriptions, request and response schemas, and try API calls from the browser. To call a protected endpoint:

1. Use `POST /api/auth/register` or `POST /api/auth/login` to obtain a JWT. The login response includes the token.
2. Select **Authorize** in Swagger UI and paste the token value into the `bearerAuth` field. Do not add the `Bearer ` prefix; the configured HTTP bearer scheme adds it to requests.
3. Use **Authorize** to apply the token, then try the authenticated endpoints. Book creation, updates, and deletion require a token with `ADMIN` authority.

Authentication and registration, book catalogue reads, and the OpenAPI documentation endpoints are public. Cart, order, payment, and book-management operations are protected as described in the endpoint table below. The interactive docs are enabled by default through `springdoc.swagger-ui.enabled=true` and `springdoc.api-docs.enabled=true` in `application.properties`. For production deployments, review whether public API documentation is appropriate and disable it when required by setting both properties to `false`.

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
| POST | `/api/payments/orders/{orderId}` | Authenticated | Initiate a demo payment for an existing order |
| GET | `/api/payments/orders/{orderId}` | Authenticated | Read payment status for an order |

### Demo payment flow

Checkout creates an order first. The authenticated customer can then initiate one payment attempt for that order by sending one of `CASH_ON_DELIVERY`, `UPI`, `CREDIT_CARD`, or `DEBIT_CARD` as the `method` in the request body:

```http
POST /api/payments/orders/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "method": "UPI"
}
```

This implementation is a demonstration only: COD payments are recorded as `PENDING`; UPI, credit-card, and debit-card strategies return simulated `SUCCEEDED` statuses. It does not contact a payment provider or move money. Do not send or store card numbers, CVVs, UPI PINs, or other payment credentials. A production integration should use a provider-hosted/tokenized flow, verify provider callbacks, and update payment status from verified provider results.

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
- Controllers depend on service interfaces (`AuthService`, `BookService`, `CartOrderService`, `PaymentService`).
- DTOs are Java records; request validation uses Jakarta Bean Validation.
- `BigDecimal` is used for monetary values.
- JPA repositories abstract persistence. `@EntityGraph` is used for cart/book reads to avoid a common N+1 access pattern.
- Book catalogue and order history use bounded pagination (configured maximum page size 100).
- Database indexes support book title/author lookup, user email lookup, cart ownership, and order history.
- `@Version` on `Book` enables optimistic locking. If two checkouts race to decrement the same stock, the losing transaction should fail and roll back rather than silently oversell inventory. Clients should treat HTTP 409 as a retryable conflict.
- Checkout is transactional and snapshots each order item's title, unit price, and quantity, so historical orders do not depend on future price/title changes.
- Payment initiation is separate from checkout. A factory selects a strategy for each payment method, and one payment record is allowed per order. The current strategies are demo implementations, not real payment processing.
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

The test suite includes service tests for book pagination/mapping, authentication, checkout, and payment strategies, plus an H2 persistence test for payment ownership queries. Run with `mvn clean test`.

## Production hardening still required

This is a learning/assignment project, not a fully production-ready commerce platform. Before production, add migration-managed schema (Flyway/Liquibase), persistent managed signing keys, refresh/revocation policy, rate limiting, audit logging, admin provisioning/authorization policy, provider-backed payments with verified callbacks, integration tests against the chosen production database, observability, and a deliberate checkout concurrency/retry policy. If the app needs multi-instance operation, use a shared database and centralized key management.
