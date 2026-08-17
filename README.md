# E-Commerce Backend API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A role-based e-commerce REST API built with Java and Spring Boot. It provides customer, seller, and administrator workflows for authentication, product approval, shopping carts, addresses, checkout, orders, inventory, payments, and account management.

## Highlights

- Stateless JWT authentication with BCrypt password hashing
- Role-based access control for customers, sellers, and administrators
- Public product browsing with search, category, price, stock, sorting, and pagination filters
- Seller-owned product management with image upload and CSV bulk import
- Administrator product approval/rejection and complete marketplace management
- Cart, address, checkout, stock reduction, cancellation, and stock restoration
- Multi-seller order support with seller-specific item status progression
- Cash on delivery and Razorpay online payments
- Razorpay signature verification, captured-payment verification, and webhooks
- Forgot/reset password flow with optional Gmail API delivery
- Bean validation and centralized REST exception handling
- OpenAPI documentation with Swagger UI
- MySQL persistence using Spring Data JPA and Hibernate
- Initial category seeding
- Service-layer unit testing with JUnit and Mockito

## Roles and Permissions

| Role | Main capabilities |
|---|---|
| `ROLE_USER` | Browse products, manage profile, addresses and cart, checkout, view/cancel own orders, and pay online |
| `ROLE_SELLER` | Manage owned products and stock, upload images, import CSV products, view earnings and seller orders, and advance item status |
| `ROLE_ADMIN` | Dashboard access, user and role management, seller oversight, category/product CRUD, product approval/rejection, and order status management |

Protected endpoints require a JWT bearer token:

```http
Authorization: Bearer <JWT_TOKEN>
```

## Core Workflows

### Product approval

```text
Seller creates or updates product
              |
              v
           PENDING
          /       \
         v         v
    APPROVED    REJECTED
```

Only approved products appear in public product browsing.

### Checkout

```text
Cart + owned address
        |
        v
Validate approved products and stock
        |
        v
Create order and order items
        |
        v
Reduce stock + create payment + clear cart
```

When an eligible order is cancelled, item stock is restored. A successful payment is marked as refunded in the local payment record when the order is cancelled.

### Seller item status

```text
PLACED -> CONFIRMED -> PACKED -> SHIPPED -> DELIVERED
```

A seller can update only items belonging to their products. The aggregate order status is synchronized from its item statuses.

## API Reference

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a customer account |
| POST | `/api/auth/login` | Public | Authenticate and receive a JWT |
| POST | `/api/auth/forgot-password` | Public | Request a password-reset token/link |
| POST | `/api/auth/reset-password` | Public | Reset a password with a valid token |
| POST | `/api/auth/logout` | Authenticated | Log out the current user |

### User Profile

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/users/me` | Authenticated | Get the current profile |
| PUT | `/api/users/me` | Authenticated | Update the current profile |
| PUT | `/api/users/change-password` | Authenticated | Change the current password |

### Public Products

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/products` | Public | Browse approved products |
| GET | `/api/products/{id}` | Public | Get an approved product |
| POST | `/api/products` | Seller/Admin | Create a product |
| PUT | `/api/products/{id}` | Owner/Admin | Update a product |
| DELETE | `/api/products/{id}` | Owner/Admin | Delete a product |
| PATCH | `/api/products/{id}/approve` | Admin | Approve a product |
| PATCH | `/api/products/{id}/reject` | Admin | Reject a product |

Product browsing supports these query parameters:

| Parameter | Purpose |
|---|---|
| `page`, `size` | Pagination |
| `q` | Name/keyword search |
| `categoryId` | Category filter |
| `minPrice`, `maxPrice` | Price range |
| `inStock` | Show only available products |
| `sort` | Sorting option; default is `newest` |

### Categories

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/categories` | Public |
| GET | `/api/categories/{id}` | Public |
| POST | `/api/categories` | Admin |
| PUT | `/api/categories/{id}` | Admin |
| DELETE | `/api/categories/{id}` | Admin |

### Cart

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | Get the current user's cart |
| POST | `/api/cart/items` | Add an item |
| PUT | `/api/cart/items/{id}` | Change item quantity |
| DELETE | `/api/cart/items/{id}` | Remove an item |
| DELETE | `/api/cart/clear` | Clear the cart |

### Addresses

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/addresses` | Create an address |
| GET | `/api/addresses` | List owned addresses |
| GET | `/api/addresses/{id}` | Get an owned address |
| PUT | `/api/addresses/{id}` | Update an owned address |
| DELETE | `/api/addresses/{id}` | Delete an owned address |

### Customer Orders

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Checkout the current cart |
| GET | `/api/orders/my` | List the current user's orders |
| GET | `/api/orders/{id}` | Get an owned order |
| DELETE | `/api/orders/{id}` | Cancel an eligible order |

Order statuses:

`PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`

### Payments

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/payments/orders/{orderId}` | Order owner | Get payment details |
| POST | `/api/payments/orders/{orderId}/razorpay` | Order owner | Create/reuse a Razorpay order |
| POST | `/api/payments/orders/{orderId}/razorpay/verify` | Order owner | Verify signature and captured status |
| POST | `/api/payments/razorpay/webhook` | Public webhook | Process signed payment events |

Payment methods: `COD`, `ONLINE`

Payment statuses: `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`

### Seller

All routes use the `/api/seller` prefix and require `ROLE_SELLER` or `ROLE_ADMIN`.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/dashboard` | Seller statistics |
| GET | `/earnings` | Seller earnings summary |
| GET/PUT | `/profile` | View or update seller profile |
| GET/POST | `/products` | List or create owned products |
| PUT/DELETE | `/products/{id}` | Update or delete an owned product |
| PUT | `/products/{id}/stock` | Update owned-product stock |
| POST | `/products/image` | Upload JPG, PNG, WEBP, or GIF |
| POST | `/products/import` | Import products from CSV |
| GET | `/orders` | List orders containing seller products |
| GET | `/orders/{id}` | Get a seller-specific order view |
| PUT | `/orders/{orderId}/items/{itemId}/status` | Advance an owned order item |
| PATCH | `/orders/{orderItemId}/status` | Advance an owned order item |

CSV import requires these headers:

```csv
name,sku,price,stock,categoryId,description,imageUrl
```

### Administrator

All routes use the `/api/admin` prefix and require `ROLE_ADMIN`.

- Dashboard statistics
- Paginated user CRUD
- Enable/disable users and update roles
- Paginated seller listing and seller overview
- View seller products and orders
- Product CRUD and approval/rejection
- Category CRUD
- View all orders and update order status

Use Swagger UI for the complete request and response schemas.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 4.1.0 | Application framework |
| Spring Web MVC | REST controllers |
| Spring Security | Authentication and authorization |
| JJWT 0.12.6 | JWT creation and validation |
| BCrypt | Password hashing |
| Spring Data JPA | Repository/data-access layer |
| Hibernate | ORM and entity mapping |
| MySQL | Relational database |
| Jakarta Validation | Request validation |
| Jackson JSR-310 | Java date/time JSON support |
| Springdoc OpenAPI | API documentation |
| Thymeleaf | Server-side view dependency |
| Java HTTP Client | Razorpay and Gmail API requests |
| Maven Wrapper | Build and execution |
| JUnit 5 and Mockito | Automated testing |
| Lombok | Boilerplate reduction |

## Project Structure

```text
src/
├── main/
│   ├── java/com/ecommerce/sufi/
│   │   ├── config/       # OpenAPI, Jackson, uploads, pagination, seed data
│   │   ├── controller/   # REST and page controllers
│   │   ├── dto/          # Validated request/response objects
│   │   ├── exception/    # Domain errors and global exception handling
│   │   ├── model/        # JPA entities and enums
│   │   ├── repo/         # Spring Data JPA repositories
│   │   ├── security/     # JWT filter, token service and security configuration
│   │   └── services/     # Business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/ecommerce/sufi/
        └── services/
```

## Getting Started

### Prerequisites

- Java 17
- MySQL 8+
- Git
- No separate Maven installation is required when using the included wrapper

### 1. Clone the repository

```bash
git clone https://github.com/mdsufidev/ecommercebackend.git
cd ecommercebackend
```

### 2. Create the database

```sql
CREATE DATABASE ecommercedb;
```

Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`, so application tables are created or updated at startup.

The application also seeds ten default product categories when they do not already exist.

### 3. Configure required environment variables

Linux/macOS:

```bash
export DB_USERNAME="root"
export DB_PASSWORD="your-mysql-password"
export JWT_SECRET="replace-with-a-long-random-secret-at-least-32-bytes"
```

Windows PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-mysql-password"
$env:JWT_SECRET="replace-with-a-long-random-secret-at-least-32-bytes"
```

> A `.env` file is not loaded automatically by Spring Boot. Export the variables in your shell, configure them in your IDE, or use your deployment platform's secret manager.

### 4. Optional integrations

#### Razorpay

```bash
export RAZORPAY_KEY_ID="rzp_test_xxxxx"
export RAZORPAY_KEY_SECRET="your-key-secret"
export RAZORPAY_WEBHOOK_SECRET="your-webhook-secret"
```

The integration creates Razorpay orders server-side, verifies the checkout signature, fetches the payment from Razorpay, and accepts only captured payments with the expected amount and order ID.

Configure the webhook URL as:

```text
https://your-domain.example/api/payments/razorpay/webhook
```

Supported events are `payment.captured` and `payment.failed`.

#### Gmail password-reset delivery

The password-reset mail service reads these properties from environment variables through Spring's relaxed binding:

```bash
export GMAIL_API_ENABLED="true"
export GMAIL_API_CLIENT_ID="your-google-oauth-client-id"
export GMAIL_API_CLIENT_SECRET="your-google-oauth-client-secret"
export GMAIL_API_REFRESH_TOKEN="your-oauth-refresh-token"
export GMAIL_API_SENDER="your-sender@gmail.com"
export APP_PUBLIC_BASE_URL="http://localhost:8080"
export PASSWORD_RESET_EXPOSE_TOKEN="false"
```

The OAuth grant must allow Gmail message sending. Keep reset-token exposure disabled outside local development.

#### Gemini

```bash
export GEMINI_API_KEY="your-api-key"
```

The dependency and configuration entry are present for Gemini-based functionality.

### 5. Run the application

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
mvnw.cmd spring-boot:run
```

Application URL:

```text
http://localhost:8080
```

### 6. Open API documentation

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Use the Swagger **Authorize** button with the JWT returned by the login endpoint.

## Example Requests

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Muhammad Sufiyan",
  "email": "sufi@example.com",
  "password": "StrongPassword123",
  "phone": "9876543210"
}
```

New registrations receive the `ROLE_USER` role by default.

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "sufi@example.com",
  "password": "StrongPassword123"
}
```

### Create a product

```json
{
  "name": "Wireless Headphones",
  "description": "Bluetooth over-ear headphones",
  "price": 2499.00,
  "stock": 20,
  "sku": "AUDIO-001",
  "imageUrl": "https://example.com/headphones.jpg",
  "categoryId": 1
}
```

Seller-created products enter the `PENDING` state. Administrator-created products are approved automatically. SKU values must be unique.

### Checkout

```json
{
  "addressId": 1,
  "paymentMethod": "ONLINE"
}
```

Use `COD` for cash on delivery or `ONLINE` for Razorpay.

## Testing

Run the automated test suite:

```bash
./mvnw test
```

The current test suite includes service-level coverage for customer registration, email normalization, BCrypt password handling, and default role assignment.

## Security Notes

- Passwords are hashed with BCrypt.
- The server does not use HTTP Basic authentication or form login.
- JWT authentication is stateless.
- Authorization is enforced through HTTP rules, method security, ownership checks, and role checks.
- Product, address, order, payment, and seller access is scoped to the authenticated owner where applicable.
- Razorpay checkout signatures and webhook signatures are validated with HMAC-SHA256.
- Online payments are marked successful only after captured status, amount, and gateway order ID are verified.
- Password-reset responses avoid revealing whether an email account exists.
- Secrets should be supplied through environment variables and must never be committed.
- Production deployments should use HTTPS, secure secret storage, restricted CORS, rate limiting, database migrations, monitoring, and expanded automated tests.

## Current Status

Implemented:

- Authentication, logout, profile management, and password changes
- Forgot/reset password with optional Gmail delivery
- Role-based security for customer, seller, and administrator
- Category CRUD and default category seeding
- Product CRUD, ownership, search/filter/sort/pagination, and approval workflow
- Seller image upload, CSV import, inventory, dashboard, earnings, and orders
- Cart and address management
- Checkout, stock handling, customer orders, cancellation, and local refund state
- Administrator dashboards and marketplace management
- COD and Razorpay payment workflows
- OpenAPI/Swagger documentation
- Validation, exception handling, and initial unit tests

Possible next improvements:

- Increase controller, service, repository, and integration test coverage
- Add refresh-token rotation and JWT revocation persistence
- Add database migrations with Flyway or Liquibase
- Add real Razorpay refund API execution and webhook idempotency storage
- Add production email templates and delivery monitoring
- Add Docker, CI/CD, rate limiting, observability, and deployment documentation

## License

This project is licensed under the [MIT License](LICENSE).

## Author

**Muhammad Sufiyan**

- GitHub: [@mdsufidev](https://github.com/mdsufidev)
- Portfolio: [muhammadsufiyan.filewalatool.com](https://muhammadsufiyan.filewalatool.com)

---

If this project helps you, consider giving the repository a star.
