# E-Commerce Backend API

A RESTful e-commerce backend built with **Java 17, Spring Boot, Spring Security, JWT, Spring Data JPA, Hibernate, and MySQL**.

The project is designed as a role-based e-commerce backend with authentication, category management, product management, seller ownership, and administrator approval workflows.

## 🚀 Features

- User registration and login
- JWT-based authentication
- Role-based authorization
- Admin-only category CRUD operations
- Product CRUD operations
- Seller ownership for products
- Admin product approval/rejection workflow
- MySQL database integration
- JPA/Hibernate entity relationships
- RESTful API architecture
- Request validation support
- Maven project structure

## 🔐 Authentication & Authorization

The API uses **JWT Bearer tokens** for stateless authentication.

Supported roles include:

- `ROLE_USER` — normal customer
- `ROLE_SELLER` — seller/product owner
- `ROLE_ADMIN` — administrator

Send the JWT token with protected requests:

```http
Authorization: Bearer <your-jwt-token>
```

### Category permissions

| Operation | Access |
|---|---|
| GET categories | Authenticated users |
| POST category | Admin only |
| PUT category | Admin only |
| DELETE category | Admin only |

### Product permissions

| Operation | Access |
|---|---|
| GET products | Public |
| POST product | Seller / Admin |
| PUT product | Product owner / Admin |
| DELETE product | Product owner / Admin |
| PATCH approve | Admin only |
| PATCH reject | Admin only |

## 📡 API Endpoints

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
```

### Categories

```text
GET    /api/categories
GET    /api/categories/{id}
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

### Products

```text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
PATCH  /api/products/{id}/approve
PATCH  /api/products/{id}/reject
```

## 🏗️ Architecture

The application follows a layered Spring Boot architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
JPA / Hibernate
    ↓
MySQL
```

Security flow:

```text
Client
  ↓
Login
  ↓
JWT Token
  ↓
Authorization: Bearer <token>
  ↓
JwtAuthFilter
  ↓
Spring Security
  ↓
Role / Permission Check
  ↓
Controller
```

## 🗃️ Main Domain Model

```text
User
 ├── Roles
 └── Products (seller/owner)

Category
 └── Products

Product
 ├── Category
 ├── Seller/User
 └── Status
```

Product status supports the approval workflow, such as:

```text
PENDING → APPROVED
PENDING → REJECTED
```

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Programming language |
| Spring Boot 4.1.0 | Backend framework |
| Spring Web MVC | REST API development |
| Spring Security | Authentication & authorization |
| JJWT 0.12.6 | JWT generation and validation |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Dependency/build management |
| Lombok | Boilerplate reduction |
| Postman | API testing |
| Git & GitHub | Version control |

## ⚙️ Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/mdsufidev/ecommercebackend.git
cd ecommercebackend
```

### 2. Configure MySQL

Create a MySQL database, for example:

```sql
CREATE DATABASE ecommerce;
```

Configure your database credentials and JWT properties in your Spring Boot configuration.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

jwt.secret=YOUR_LONG_SECRET_KEY
jwt.expiration=86400000
```

> Do not commit real database passwords, JWT secrets, API keys, or other credentials to GitHub.

### 3. Run the application

Using Maven Wrapper on Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

The API runs by default at:

```text
http://localhost:8080
```

## 🧪 Example Login Flow

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Muhammad Sufiyan",
  "email": "sufi@example.com",
  "password": "your-password",
  "phone": "9876543210"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

After successful login, use the returned JWT token for protected endpoints:

```http
Authorization: Bearer <JWT_TOKEN>
```

## 📦 Example Product Request

```json
{
  "name": "Samsung Galaxy S25",
  "description": "Latest Samsung smartphone",
  "price": 74999.00,
  "stock": 25,
  "sku": "SAM-S25-001",
  "imageUrl": "https://example.com/s25.jpg",
  "categoryId": 1
}
```

## 📋 Project Status

### Implemented

- Authentication
- JWT security
- Role-based authorization
- User roles
- Category management
- Product management
- Product approval/rejection
- Seller ownership

### Planned

- Shopping cart
- Order management
- Payment integration
- Advanced product search/filtering
- Pagination and sorting improvements
- Automated unit/integration test coverage
- API documentation with OpenAPI/Swagger

## 🔒 Security Notes

This project is intended for learning and development. Before using it in production, additional security hardening should be performed, including:

- Password hashing with BCrypt/Argon2
- Secure secret management using environment variables or a secret manager
- Refresh-token strategy
- Strong request validation
- Global exception handling
- Rate limiting
- Production database configuration
- HTTPS

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Make your changes.
4. Test the changes.
5. Submit a pull request.

## 📄 License

This project is licensed under the **MIT License**. See the `LICENSE` file for details.

## 👨‍💻 Author

**Muhammad Sufiyan**

GitHub: https://github.com/mdsufidev

---

⭐ If you find this project useful, consider giving it a star on GitHub.
