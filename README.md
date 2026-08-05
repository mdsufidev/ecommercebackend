# 🛒 E-Commerce Backend API

A production-oriented **E-Commerce Backend REST API** built with **Java and Spring Boot**. The project focuses on clean backend architecture, JWT-based authentication, role-based authorization, product and category management, seller ownership, and an admin approval workflow.

> 🚧 This project is actively being developed. Cart, order, and payment modules are planned as the next stages of development.

## ✨ Features

### 🔐 Authentication & Authorization

- User registration and login
- JWT-based stateless authentication
- Role-based authorization using Spring Security
- `ROLE_ADMIN`, `ROLE_SELLER`, and user-level access
- Protected API endpoints
- Admin-only category management
- Authenticated seller/product operations

### 📂 Category Management

- Create category
- Get all categories
- Get category by ID
- Update category
- Delete category
- Category CRUD restricted to administrators

### 📦 Product Management

- Create products
- Get all products
- Get product by ID
- Update products
- Delete products
- Product SKU management
- Stock management
- Product-to-category relationship
- Product-to-seller relationship
- Seller ownership checks
- Admin product approval/rejection workflow

### 🔄 Product Approval Workflow

```text
Seller creates product
        ↓
     PENDING
        ↓
   Admin reviews
     ↙       ↘
APPROVED    REJECTED
    ↓
Available to customers
```

## 🏗️ Architecture

The application follows a layered Spring Boot architecture:

```text
Client / Postman
       │
       ▼
 REST Controller
       │
       ▼
    Service
       │
       ▼
  Repository
       │
       ▼
     MySQL
```

Security flow:

```text
Login
  ↓
JWT generated
  ↓
Client sends Bearer Token
  ↓
JwtAuthFilter
  ↓
Token validation + role extraction
  ↓
Spring Security authorization
  ↓
Controller
```

## 🧩 Project Structure

```text
src/main/java/com/ecommerce/sufi/
│
├── controller/
│   ├── AuthController.java
│   ├── CategoryController.java
│   └── ProductController.java
│
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   ├── CategoryRequest.java
│   └── ProductRequest.java
│
├── model/
│   ├── User.java
│   ├── Role.java
│   ├── RoleName.java
│   ├── Category.java
│   └── Product.java
│
├── repo/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── CategoryRepository.java
│   └── ProductRepository.java
│
├── services/
│   ├── UserService.java
│   ├── UserServiceImpl.java
│   ├── CategoryService.java
│   ├── CategoryServiceImpl.java
│   ├── ProductService.java
│   └── ProductServiceImpl.java
│
└── security/
    ├── SecurityConfig.java
    ├── JwtService.java
    └── JwtAuthFilter.java
```

## 🔑 API Endpoints

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| `POST` | `/api/auth/register` | Public |
| `POST` | `/api/auth/login` | Public |

### Categories

| Method | Endpoint | Access |
|---|---|---|
| `GET` | `/api/categories` | Authenticated |
| `GET` | `/api/categories/{id}` | Authenticated |
| `POST` | `/api/categories` | Admin |
| `PUT` | `/api/categories/{id}` | Admin |
| `DELETE` | `/api/categories/{id}` | Admin |

### Products

| Method | Endpoint | Access |
|---|---|---|
| `GET` | `/api/products` | Public |
| `GET` | `/api/products/{id}` | Public |
| `POST` | `/api/products` | Authenticated Seller/Admin |
| `PUT` | `/api/products/{id}` | Owner/Admin |
| `DELETE` | `/api/products/{id}` | Owner/Admin |
| `PATCH` | `/api/products/{id}/approve` | Admin |
| `PATCH` | `/api/products/{id}/reject` | Admin |

### Planned Modules

```text
/api/cart
/api/orders
/api/payments
```

These modules will be implemented in upcoming development stages.

## 🗃️ Core Entity Relationships

```text
User ───────< User Roles
 │
 │ seller
 ▼
Product >──── Category
```

A product belongs to a category and is associated with the seller who created it. Users can have one or more roles through the user-role relationship.

## 🛡️ Security

The API uses **Spring Security + JWT** for stateless authentication and authorization.

Example protected request:

```http
Authorization: Bearer <JWT_TOKEN>
```

Administrative operations use role-based authorization such as:

```java
.hasRole("ADMIN")
```

The JWT contains the authenticated user's email and role information, which is used by the security filter and authorization layer.

## 🧰 Tech Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming language |
| Spring Boot | Application framework |
| Spring Security | Authentication & authorization |
| JWT | Stateless authentication |
| Spring Data JPA | Data access |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Dependency management & build |
| Postman | REST API testing |
| Git | Version control |
| GitHub | Source code & collaboration |

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/mdsufidev/ecommercebackend.git
cd ecommercebackend
```

### 2. Configure MySQL

Create a MySQL database and configure your local application settings.

Example configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

jwt.secret=YOUR_JWT_SECRET
jwt.expiration=86400000
```

> ⚠️ Never commit real database passwords, JWT secrets, API keys, or other credentials to GitHub.

### 3. Run the application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

## 🧪 Testing with Postman

Recommended development flow:

```text
Register User
    ↓
Login
    ↓
Receive JWT
    ↓
Send JWT in Authorization header
    ↓
Access protected APIs
```

For protected requests, use:

```text
Authorization → Bearer Token → <your JWT>
```

## 📌 Development Roadmap

- [x] User registration
- [x] User login
- [x] JWT authentication
- [x] Role-based authorization
- [x] Category CRUD
- [x] Product CRUD
- [x] Seller ownership
- [x] Product approval/rejection
- [ ] Cart module
- [ ] Order module
- [ ] Payment module
- [ ] Password hashing with BCrypt
- [ ] Global exception handling improvements
- [ ] API documentation with Swagger/OpenAPI
- [ ] Automated unit and integration tests
- [ ] Docker support
- [ ] CI/CD pipeline

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Commit your changes
5. Push the branch
6. Open a Pull Request

Please keep changes focused and include appropriate tests where possible.

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Muhammad Sufiyan**

Java Backend Developer | Spring Boot | REST APIs | MySQL

- GitHub: https://github.com/mdsufidev

---

⭐ If you find this project useful, consider giving it a star and following the repository for future updates.
