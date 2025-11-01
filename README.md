# E-Commerce Microservices Application - Complete Documentation

A comprehensive, production-ready e-commerce application built with Spring Boot microservices architecture and React.js frontend, implementing advanced microservice patterns including service discovery, API gateway, circuit breakers, event-driven architecture, and distributed tracing.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Technologies Used](#technologies-used)
4. [System Architecture](#system-architecture)
5. [Microservices Details](#microservices-details)
6. [Database Configuration](#database-configuration)
7. [Prerequisites & Installation](#prerequisites--installation)
8. [Complete Setup Guide](#complete-setup-guide)
9. [API Documentation](#api-documentation)
10. [Microservices Patterns](#microservices-patterns)
11. [Circuit Breaker Implementation](#circuit-breaker-implementation)
12. [Distributed Tracing with Zipkin](#distributed-tracing-with-zipkin)
13. [Event-Driven Architecture (Kafka)](#event-driven-architecture-kafka)
14. [Frontend Application](#frontend-application)
15. [Database Seeding](#database-seeding)
16. [Testing](#testing)
17. [Troubleshooting](#troubleshooting)
18. [Production Considerations](#production-considerations)
19. [Project Structure](#project-structure)

---

## 🎯 Project Overview

This is a full-stack e-commerce application demonstrating microservices architecture with the following key features:

- **8 Microservices** built with Spring Boot 3.2.0
- **Service Discovery** using Netflix Eureka
- **API Gateway** for routing and load balancing
- **Event-Driven Communication** using Apache Kafka
- **Circuit Breakers** using Resilience4j for fault tolerance
- **Distributed Tracing** using Zipkin and Micrometer
- **React.js Frontend** with Material-UI
- **PostgreSQL Database** for persistent storage
- **JWT Authentication** for secure access
- **Complete CRUD Operations** for all entities

---

## 🏗️ Architecture Overview

### High-Level Architecture

```
┌─────────────────┐
│  React Frontend │
│   (Port 3000)   │
└────────┬────────┘
         │
         │ HTTP Requests
         │
┌────────▼─────────────────────────────────┐
│          API Gateway                     │
│         (Port 8080)                      │
│  • Routing & Load Balancing              │
│  • CORS Configuration                    │
│  • Circuit Breakers                      │
└────────┬─────────────────────────────────┘
         │
         ├─────────────────────────────────┐
         │                                 │
┌────────▼────────┐              ┌─────────▼─────────┐
│ Service Registry│              │  Zipkin (Port 9411)│
│  Eureka (8761)  │              │  Distributed Trace │
└─────────────────┘              └────────────────────┘
         │
         │ Service Registration
         │
    ┌────┴────┬──────────────┬───────────────┬──────────────┐
    │         │              │               │              │
┌───▼───┐ ┌──▼────┐  ┌──────▼──────┐  ┌────▼─────┐  ┌─────▼─────┐
│ User  │ │Product│  │   Order     │  │ Payment  │  │ Inventory │
│Service│ │Service│  │   Service   │  │ Service  │  │ Service   │
│ 8081  │ │ 8082  │  │   8083      │  │  8084    │  │   8085    │
└───┬───┘ └───┬───┘  └──────┬──────┘  └────┬─────┘  └─────┬─────┘
    │         │             │              │              │
    │         │             │              │              │
┌───▼─────────▼─────────────▼──────────────▼──────────────▼─────┐
│                    PostgreSQL Database                         │
│                    (Port 5432)                                 │
│          • users, products, orders, payments, inventory        │
└────────────────────────────────────────────────────────────────┘
         │
         │
┌────────▼───────────────────────────────────────────────────────┐
│                    Apache Kafka (Port 9092)                    │
│              Topics: order-created, payment-completed          │
└────────┬───────────────────────────────────────────────────────┘
         │
         │ Event Consumption
         │
┌────────▼─────────┐
│  Notification    │
│    Service       │
│    (Port 8086)   │
└──────────────────┘
```

---

## 🛠️ Technologies Used

### Backend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.2.0 | Microservices framework |
| **Spring Cloud** | 2023.0.0 | Service discovery, gateway, config |
| **Spring Data JPA** | 3.2.0 | Database access layer |
| **Spring Security** | 6.2.0 | Authentication & authorization |
| **Netflix Eureka** | 4.1.0 | Service discovery & registration |
| **Spring Cloud Gateway** | 4.1.0 | API Gateway |
| **Spring Cloud OpenFeign** | 4.1.0 | REST client for inter-service calls |
| **Resilience4j** | 2.1.0 | Circuit breaker pattern |
| **JWT (jjwt)** | Latest | Token-based authentication |
| **PostgreSQL** | 42.6.0 | Relational database |
| **Apache Kafka** | 3.6.0 | Event streaming platform |
| **Micrometer Tracing** | 1.2.0 | Distributed tracing metrics |
| **Zipkin** | Latest | Distributed tracing UI |
| **Maven** | 3.6+ | Build & dependency management |
| **Hibernate Validator** | 8.0.1 | Bean validation |
| **Lombok** | - | Code generation (if used) |

### Frontend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 18 | UI library |
| **React Router** | Latest | Client-side routing |
| **Material-UI (MUI)** | Latest | UI components |
| **Axios** | Latest | HTTP client |
| **Context API** | Built-in | State management |

### Infrastructure

| Technology | Purpose |
|------------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **Zookeeper** | Kafka coordination |
| **Zipkin** | Distributed tracing storage |

---

## 🏛️ System Architecture

### Service Communication Flow

1. **Client Request Flow**:
   ```
   React App → API Gateway → Service Discovery → Target Microservice → Database
   ```

2. **Inter-Service Communication**:
   ```
   Order Service → Feign Client → Product Service
   Order Service → Kafka Producer → Kafka Topics
   Payment/Inventory/Notification Services → Kafka Consumer → Process Events
   ```

3. **Event-Driven Flow**:
   ```
   Order Created → Kafka Topic (order-created) → 
   ├──→ Payment Service (Process Payment)
   ├──→ Inventory Service (Update Stock)
   └──→ Notification Service (Send Notification)
   ```

---

## 🔧 Microservices Details

### 1. Service Registry (Eureka) - Port 8761

**Purpose**: Service discovery and registration hub for all microservices.

**Key Features**:
- Self-registration of microservices
- Health monitoring
- Load balancing information
- Service metadata storage

**Configuration**:
- **File**: `microservices/service-registry/src/main/resources/application.yml`
- **Port**: 8761
- **UI Dashboard**: http://localhost:8761

**Dependencies**:
- `spring-cloud-starter-netflix-eureka-server`

---

### 2. API Gateway - Port 8080

**Purpose**: Single entry point for all client requests, routing to appropriate microservices.

**Key Features**:
- **Route Management**: Routes requests based on path patterns
- **Load Balancing**: Uses Eureka service discovery for load balancing
- **CORS Configuration**: Handles cross-origin requests
- **Circuit Breakers**: Protects routes with Resilience4j
- **Path Rewriting**: Modifies request paths for downstream services
- **Header Management**: Removes/modifies headers as needed

**Route Configuration**:

| Route ID | Path Pattern | Target Service | Filters |
|----------|--------------|----------------|---------|
| `user-service` | `/api/users/**` | `lb://user-service` | RewritePath, RemoveCookie |
| `product-service` | `/api/products/**` | `lb://product-service` | StripPrefix |
| `order-service` | `/api/orders/**` | `lb://order-service` | StripPrefix |
| `payment-service` | `/api/payments/**` | `lb://payment-service` | StripPrefix |
| `inventory-service` | `/api/inventory/**` | `lb://inventory-service` | StripPrefix |

**Circuit Breaker Configuration**:
- `userService`: 50% failure threshold, 10s wait duration
- `productService`: 50% failure threshold, 10s wait duration
- `orderService`: 50% failure threshold
- `paymentService`: 50% failure threshold

**Key Files**:
- `microservices/api-gateway/src/main/java/com/ecommerce/gateway/ApiGatewayApplication.java`
- `microservices/api-gateway/src/main/resources/application.yml`
- `microservices/api-gateway/src/main/java/com/ecommerce/gateway/config/CorsConfig.java`

**Dependencies**:
- `spring-cloud-starter-gateway`
- `spring-cloud-starter-netflix-eureka-client`
- `resilience4j-spring-boot3`
- `spring-boot-starter-data-redis-reactive`

---

### 3. User Service - Port 8081

**Purpose**: User authentication, authorization, and user management.

**Key Features**:
- User registration with validation
- User login with JWT token generation
- Password encryption using BCrypt
- JWT token validation
- Support for login with username or email

**Database Table**: `users`

**Entity Structure**:
```java
- id: Long (Primary Key)
- username: String (Unique, NotBlank, 3-50 chars)
- email: String (Unique, Email format)
- password: String (Encrypted with BCrypt)
- firstName: String
- lastName: String
- phone: String
- address: String
- role: String (Default: "USER")
```

**Security Configuration**:
- JWT Secret: `mySecretKeyForJWTTokenGeneration12345678901234567890`
- JWT Expiration: 86400000ms (24 hours)
- BCrypt password encoding
- CORS enabled for localhost:3000 and 127.0.0.1:3000

**Key Components**:
- `AuthController`: REST endpoints for authentication
- `AuthService`: Business logic for authentication
- `UserService`: User management operations
- `JwtUtil`: JWT token generation and validation
- `SecurityConfig`: Spring Security configuration

**Key Files**:
- `microservices/user-service/src/main/java/com/ecommerce/user/controller/AuthController.java`
- `microservices/user-service/src/main/java/com/ecommerce/user/service/AuthService.java`
- `microservices/user-service/src/main/java/com/ecommerce/user/config/SecurityConfig.java`
- `microservices/user-service/src/main/java/com/ecommerce/user/util/JwtUtil.java`

**Dependencies**:
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `postgresql`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`

---

### 4. Product Service - Port 8082

**Purpose**: Product catalog management, product CRUD operations, search and filtering.

**Key Features**:
- Product catalog management
- Product search by name
- Category-based filtering
- Circuit breaker protection for inventory calls
- Full CRUD operations

**Database Table**: `products`

**Entity Structure**:
```java
- id: Long (Primary Key)
- name: String (NotBlank)
- description: String
- price: BigDecimal (DecimalMin: 0.0, exclusive)
- category: String
- imageUrl: String
- stockQuantity: Integer
```

**Circuit Breaker**:
- `inventoryService` circuit breaker configured
- Fallback method returns empty list on failure

**Key Files**:
- `microservices/product-service/src/main/java/com/ecommerce/product/controller/ProductController.java`
- `microservices/product-service/src/main/java/com/ecommerce/product/service/ProductService.java`
- `microservices/product-service/src/main/java/com/ecommerce/product/model/Product.java`

**Dependencies**:
- `spring-boot-starter-data-jpa`
- `spring-cloud-starter-openfeign`
- `resilience4j-spring-boot3`
- `spring-boot-starter-aop`
- `spring-boot-starter-validation`

---

### 5. Order Service - Port 8083

**Purpose**: Order processing, order management, integrates with Product Service.

**Key Features**:
- Order creation with multiple items
- Order status management (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- Integration with Product Service via Feign Client
- Kafka event publishing (order-created topic)
- Circuit breaker protection with fallback
- Total amount calculation

**Database Tables**: 
- `orders`
- `order_items`

**Order Entity Structure**:
```java
- id: Long (Primary Key)
- userId: Long
- status: OrderStatus (Enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- totalAmount: BigDecimal
- shippingAddress: String
- orderDate: LocalDateTime
- items: List<OrderItem>
```

**OrderItem Entity Structure**:
```java
- id: Long (Primary Key)
- order: Order (Many-to-One)
- productId: Long
- productName: String
- quantity: Integer
- price: BigDecimal
```

**Feign Client Integration**:
- `ProductClient`: Calls Product Service to fetch product details
- Circuit breaker enabled with fallback
- Fallback returns placeholder product on failure

**Circuit Breaker Configuration**:
```yaml
productService:
  slidingWindowSize: 10
  minimumNumberOfCalls: 5
  failureRateThreshold: 50%
  waitDurationInOpenState: 10000ms
  permittedNumberOfCallsInHalfOpenState: 3
```

**Kafka Integration**:
- Producer for `order-created` topic
- Publishes order events after order creation

**Key Files**:
- `microservices/order-service/src/main/java/com/ecommerce/order/controller/OrderController.java`
- `microservices/order-service/src/main/java/com/ecommerce/order/service/OrderService.java`
- `microservices/order-service/src/main/java/com/ecommerce/order/client/ProductClient.java`
- `microservices/order-service/src/main/java/com/ecommerce/order/client/ProductClientFallback.java`

**Dependencies**:
- `spring-cloud-starter-openfeign`
- `spring-cloud-starter-circuitbreaker-resilience4j`
- `spring-kafka`
- `resilience4j-spring-boot3`
- `spring-boot-starter-aop`

---

### 6. Payment Service - Port 8084

**Purpose**: Payment processing and transaction management.

**Key Features**:
- Payment processing
- Payment status tracking
- Kafka event consumption (order-created)
- Payment lookup by order ID

**Database Table**: `payments`

**Entity Structure**:
```java
- id: Long (Primary Key)
- orderId: Long
- userId: Long
- amount: BigDecimal
- paymentMethod: String
- status: PaymentStatus (Enum: PENDING, COMPLETED, FAILED)
- transactionId: String
- paymentDate: LocalDateTime
```

**Kafka Integration**:
- Consumer for `order-created` topic
- Automatically processes payments when orders are created

**Key Files**:
- `microservices/payment-service/src/main/java/com/ecommerce/payment/controller/PaymentController.java`
- `microservices/payment-service/src/main/java/com/ecommerce/payment/service/PaymentService.java`

**Dependencies**:
- `spring-kafka`
- `spring-boot-starter-data-jpa`

---

### 7. Inventory Service - Port 8085

**Purpose**: Stock management, inventory tracking, and reservation.

**Key Features**:
- Inventory tracking per product
- Inventory reservation
- Available quantity calculation
- Kafka event consumption (order-created)
- Real-time stock updates

**Database Table**: `inventory`

**Entity Structure**:
```java
- id: Long (Primary Key)
- productId: Long
- quantity: Integer (Min: 0)
- reservedQuantity: Integer (Default: 0)
- availableQuantity: Integer (Calculated: quantity - reservedQuantity)
```

**Kafka Integration**:
- Consumer for `order-created` topic
- Automatically reserves inventory when orders are created

**Key Files**:
- `microservices/inventory-service/src/main/java/com/ecommerce/inventory/controller/InventoryController.java`
- `microservices/inventory-service/src/main/java/com/ecommerce/inventory/service/InventoryService.java`

**Dependencies**:
- `spring-kafka`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`

---

### 8. Notification Service - Port 8086

**Purpose**: Asynchronous notification handling.

**Key Features**:
- Email notifications (simulated)
- SMS notifications (simulated)
- Kafka event consumption
- Asynchronous message processing

**Kafka Integration**:
- Consumer for multiple Kafka topics
- Processes notifications asynchronously

**Key Files**:
- `microservices/notification-service/src/main/java/com/ecommerce/notification/service/NotificationService.java`

**Dependencies**:
- `spring-kafka`

---

## 🗄️ Database Configuration

### PostgreSQL Setup

**Connection Details**:
- **Host**: localhost
- **Port**: 5432
- **Database**: postgres
- **Username**: ujjawalkumar
- **Password**: ujju

### Database Tables

Each microservice creates and manages its own tables:

| Service | Tables | Purpose |
|---------|--------|---------|
| **User Service** | `users` | User accounts and authentication |
| **Product Service** | `products` | Product catalog |
| **Order Service** | `orders`, `order_items` | Order records and items |
| **Payment Service** | `payments` | Payment transactions |
| **Inventory Service** | `inventory` | Stock management |

### Hibernate Configuration

All services use:
- **DDL Auto**: `update` (automatically creates/updates tables)
- **Show SQL**: `true` (SQL queries visible in logs)
- **Dialect**: `PostgreSQLDialect`
- **Format SQL**: `true` (formatted SQL output)

### Database Initialization

Tables are automatically created when services start for the first time. Hibernate will:
1. Check if tables exist
2. Create tables if they don't exist
3. Update schema if entity changes (when `ddl-auto: update`)

---

## 📦 Prerequisites & Installation

### Required Software

1. **Java 17 or Higher**
   ```bash
   # macOS
   brew install openjdk@17
   
   # Verify
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   # macOS
   brew install maven
   
   # Verify
   mvn -version
   ```

3. **Node.js 16+ and npm**
   ```bash
   # macOS
   brew install node
   
   # Verify
   node -version
   npm -version
   ```

4. **PostgreSQL**
   ```bash
   # macOS
   brew install postgresql@14
   brew services start postgresql@14
   
   # Verify
   psql -U ujjawalkumar -d postgres
   ```

5. **Docker Desktop** (for Kafka, Zookeeper, Zipkin)
   ```bash
   # macOS
   brew install --cask docker
   # Then start Docker Desktop application
   ```

### Verify Installation

```bash
# Check all prerequisites
java -version      # Should show Java 17+
mvn -version       # Should show Maven 3.6+
node -version      # Should show Node 16+
npm -version       # Should show npm 8+
psql --version     # Should show PostgreSQL version
docker --version   # Should show Docker version
```

---

## 🚀 Complete Setup Guide

### Step 1: Start PostgreSQL

```bash
# macOS
brew services start postgresql@14

# Verify PostgreSQL is running
psql -h localhost -U ujjawalkumar -d postgres
# Password: ujju
```

### Step 2: Start Kafka, Zookeeper, and Zipkin

```bash
cd microservices
docker-compose up -d zookeeper kafka zipkin

# Wait 30 seconds for services to be ready
# Verify containers are running
docker ps | grep -E "zookeeper|kafka|zipkin"
```

**Services Available**:
- **Kafka**: localhost:9092
- **Zipkin UI**: http://localhost:9411

### Step 3: Start Service Registry (Eureka)

**Terminal 1**:
```bash
cd microservices/service-registry
mvn spring-boot:run
```

Wait until you see: `Started ServiceRegistryApplication` (30-60 seconds)

**Verify**: Open http://localhost:8761 in browser

### Step 4: Start API Gateway

**Terminal 2**:
```bash
cd microservices/api-gateway
mvn spring-boot:run
```

**Verify**: Gateway starts on port 8080

### Step 5: Start User Service

**Terminal 3**:
```bash
cd microservices/user-service
mvn spring-boot:run
```

**Verify**: Service starts on port 8081, registers with Eureka

### Step 6: Start Product Service

**Terminal 4**:
```bash
cd microservices/product-service
mvn spring-boot:run
```

**Verify**: Service starts on port 8082, creates `products` table

### Step 7: Start Order Service

**Terminal 5**:
```bash
cd microservices/order-service
mvn spring-boot:run
```

**Verify**: Service starts on port 8083, creates `orders` and `order_items` tables

### Step 8: Start Payment Service

**Terminal 6**:
```bash
cd microservices/payment-service
mvn spring-boot:run
```

**Verify**: Service starts on port 8084, creates `payments` table

### Step 9: Start Inventory Service

**Terminal 7**:
```bash
cd microservices/inventory-service
mvn spring-boot:run
```

**Verify**: Service starts on port 8085, creates `inventory` table

### Step 10: Start Notification Service

**Terminal 8**:
```bash
cd microservices/notification-service
mvn spring-boot:run
```

**Verify**: Service starts on port 8086

### Step 11: Start React Frontend

**Terminal 9**:
```bash
cd microservices/ecommerce-ui
npm install  # Only needed first time
npm start
```

**Verify**: Frontend opens at http://localhost:3000

### Quick Start Script (Alternative)

Create `start-all.sh`:
```bash
#!/bin/bash

# Start Kafka, Zookeeper, Zipkin
cd microservices
docker-compose up -d zookeeper kafka zipkin
sleep 30

# Start Eureka
cd service-registry
mvn spring-boot:run > /tmp/eureka.log 2>&1 &
sleep 30

# Start all services in background
cd ../api-gateway && mvn spring-boot:run > /tmp/api-gateway.log 2>&1 &
cd ../user-service && mvn spring-boot:run > /tmp/user-service.log 2>&1 &
cd ../product-service && mvn spring-boot:run > /tmp/product-service.log 2>&1 &
cd ../order-service && mvn spring-boot:run > /tmp/order-service.log 2>&1 &
cd ../payment-service && mvn spring-boot:run > /tmp/payment-service.log 2>&1 &
cd ../inventory-service && mvn spring-boot:run > /tmp/inventory-service.log 2>&1 &
cd ../notification-service && mvn spring-boot:run > /tmp/notification-service.log 2>&1 &

# Start frontend
cd ecommerce-ui
npm start
```

Make executable: `chmod +x start-all.sh`

---

## 📡 API Documentation

All APIs are accessible through the API Gateway at `http://localhost:8080/api/...`

### User Service APIs

#### 1. Register User
- **Endpoint**: `POST /api/users/auth/register`
- **Description**: Register a new user account
- **Request Body**:
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "address": "123 Main St"
}
```
- **Response** (201 Created):
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "User registered successfully"
}
```

#### 2. Login User
- **Endpoint**: `POST /api/users/auth/login`
- **Description**: Authenticate user and get JWT token
- **Request Body**:
```json
{
  "username": "johndoe",  // Can use username or email
  "password": "password123"
}
```
- **Response** (200 OK):
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful"
}
```
- **Error Response** (401 Unauthorized):
```json
{
  "error": "Invalid credentials"
}
```

#### 3. Get All Users
- **Endpoint**: `GET /api/users/auth/users`
- **Description**: Get list of all users (requires authentication)
- **Response** (200 OK):
```json
[
  {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
]
```

---

### Product Service APIs

#### 1. Get All Products
- **Endpoint**: `GET /api/products`
- **Description**: Retrieve all products
- **Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "category": "Electronics",
    "imageUrl": "https://example.com/laptop.jpg",
    "stockQuantity": 50
  }
]
```

#### 2. Get Product by ID
- **Endpoint**: `GET /api/products/{id}`
- **Description**: Get specific product details
- **Path Parameter**: `id` (Long)
- **Response** (200 OK):
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "category": "Electronics",
  "imageUrl": "https://example.com/laptop.jpg",
  "stockQuantity": 50
}
```
- **Error Response** (404 Not Found): When product doesn't exist

#### 3. Get Products by Category
- **Endpoint**: `GET /api/products/category/{category}`
- **Description**: Filter products by category
- **Path Parameter**: `category` (String)
- **Response**: Array of products matching category

#### 4. Search Products
- **Endpoint**: `GET /api/products/search?q={query}`
- **Description**: Search products by name
- **Query Parameter**: `q` (String) - search term
- **Response**: Array of products matching search term

#### 5. Create Product
- **Endpoint**: `POST /api/products`
- **Description**: Create a new product
- **Request Body**:
```json
{
  "name": "New Product",
  "description": "Product description",
  "price": 49.99,
  "category": "Electronics",
  "imageUrl": "https://example.com/image.jpg",
  "stockQuantity": 100
}
```
- **Response** (201 Created): Created product object

#### 6. Update Product
- **Endpoint**: `PUT /api/products/{id}`
- **Description**: Update existing product
- **Path Parameter**: `id` (Long)
- **Request Body**: Product object with updated fields
- **Response** (200 OK): Updated product object
- **Error Response** (404 Not Found): When product doesn't exist

#### 7. Delete Product
- **Endpoint**: `DELETE /api/products/{id}`
- **Description**: Delete a product
- **Path Parameter**: `id` (Long)
- **Response** (200 OK): No content

---

### Order Service APIs

#### 1. Create Order
- **Endpoint**: `POST /api/orders`
- **Description**: Create a new order
- **Request Body**:
```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "shippingAddress": "123 Main St, City, Country"
}
```
- **Response** (201 Created):
```json
{
  "id": 1,
  "userId": 1,
  "status": "PENDING",
  "totalAmount": 2049.97,
  "shippingAddress": "123 Main St, City, Country",
  "orderDate": "2024-01-01T12:00:00",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop",
      "quantity": 2,
      "price": 999.99
    },
    {
      "id": 2,
      "productId": 2,
      "productName": "Mouse",
      "quantity": 1,
      "price": 49.99
    }
  ]
}
```
- **Process**: 
  1. Fetches product details via Feign Client
  2. Calculates total amount
  3. Saves order to database
  4. Publishes `order-created` event to Kafka

#### 2. Get Orders by User
- **Endpoint**: `GET /api/orders/user/{userId}`
- **Description**: Get all orders for a specific user
- **Path Parameter**: `userId` (Long)
- **Response** (200 OK): Array of orders

#### 3. Get Order by ID
- **Endpoint**: `GET /api/orders/{id}`
- **Description**: Get specific order details
- **Path Parameter**: `id` (Long)
- **Response** (200 OK): Order object
- **Error Response** (404 Not Found): When order doesn't exist

#### 4. Update Order Status
- **Endpoint**: `PUT /api/orders/{id}/status?status={status}`
- **Description**: Update order status
- **Path Parameter**: `id` (Long)
- **Query Parameter**: `status` (OrderStatus enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- **Response** (200 OK): Updated order object

---

### Payment Service APIs

#### 1. Process Payment
- **Endpoint**: `POST /api/payments`
- **Description**: Process a payment for an order
- **Request Body**:
```json
{
  "orderId": 1,
  "userId": 1,
  "amount": 2049.97,
  "paymentMethod": "CREDIT_CARD"
}
```
- **Response** (201 Created):
```json
{
  "id": 1,
  "orderId": 1,
  "userId": 1,
  "amount": 2049.97,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "transactionId": "TXN-123456",
  "paymentDate": "2024-01-01T12:00:00"
}
```

#### 2. Get Payment by ID
- **Endpoint**: `GET /api/payments/{id}`
- **Description**: Get specific payment details
- **Path Parameter**: `id` (Long)
- **Response** (200 OK): Payment object

#### 3. Get Payments by Order
- **Endpoint**: `GET /api/payments/order/{orderId}`
- **Description**: Get all payments for an order
- **Path Parameter**: `orderId` (Long)
- **Response** (200 OK): Array of payments

**Note**: Payment Service also automatically processes payments when it consumes `order-created` events from Kafka.

---

### Inventory Service APIs

#### 1. Get Inventory by Product
- **Endpoint**: `GET /api/inventory/product/{productId}`
- **Description**: Get inventory information for a product
- **Path Parameter**: `productId` (Long)
- **Response** (200 OK):
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 50,
  "reservedQuantity": 5,
  "availableQuantity": 45
}
```

#### 2. Update Inventory
- **Endpoint**: `PUT /api/inventory/product/{productId}`
- **Description**: Update inventory quantity for a product
- **Path Parameter**: `productId` (Long)
- **Request Body**:
```json
{
  "quantity": 100
}
```
- **Response** (200 OK): Updated inventory object

#### 3. Reserve Inventory
- **Endpoint**: `POST /api/inventory/reserve`
- **Description**: Reserve inventory for an order
- **Request Body**:
```json
{
  "productId": 1,
  "quantity": 5
}
```
- **Response** (200 OK): Updated inventory object with reserved quantity

**Note**: Inventory Service also automatically reserves inventory when it consumes `order-created` events from Kafka.

---

## 🎯 Microservices Patterns

### 1. Service Discovery (Eureka)

**How It Works**:
- All microservices register with Eureka on startup
- Services query Eureka to find other services
- Load balancing across service instances
- Health monitoring and automatic deregistration

**Configuration**:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

### 2. API Gateway Pattern

**Purpose**: Single entry point for all client requests

**Benefits**:
- Centralized routing
- Cross-cutting concerns (CORS, security, logging)
- Request/response transformation
- Rate limiting (can be added)
- Load balancing

**Implementation**: Spring Cloud Gateway with route predicates and filters

---

### 3. Circuit Breaker Pattern (Resilience4j)

**Purpose**: Prevent cascading failures and provide graceful degradation

**Implementation Details**: See [Circuit Breaker Implementation](#circuit-breaker-implementation) section

---

### 4. Event-Driven Architecture (Kafka)

**Purpose**: Asynchronous communication between microservices

**Kafka Topics**:
- `order-created`: Published when order is created
  - Consumers: Payment Service, Inventory Service, Notification Service

**Event Flow**:
```
Order Service → Kafka Producer → order-created topic
                                       ↓
                    ┌──────────────────┼──────────────────┐
                    ↓                  ↓                  ↓
            Payment Service    Inventory Service  Notification Service
            (Process Payment)  (Reserve Stock)   (Send Notification)
```

**Benefits**:
- Loose coupling between services
- Asynchronous processing
- Scalability
- Event sourcing capability

---

### 5. Service Communication

**Types**:
1. **Synchronous**: REST APIs via Feign Client (Order → Product)
2. **Asynchronous**: Kafka events (Order → Payment/Inventory/Notification)

**Feign Client Example**:
```java
@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductDto getProduct(@PathVariable Long id);
}
```

---

### 6. Distributed Tracing (Zipkin)

**Purpose**: Track requests across multiple microservices

**Implementation**: Micrometer Tracing with Zipkin

**Features**:
- Trace ID propagation
- Span creation for each service call
- Parent-child span relationships
- Performance metrics
- Error tracking

See [Distributed Tracing with Zipkin](#distributed-tracing-with-zipkin) section for details.

---

## 🔄 Circuit Breaker Implementation

### Overview

Circuit breakers are implemented using **Resilience4j** to provide fault tolerance and graceful degradation.

### Implementation Locations

#### 1. Order Service - Product Client

**File**: `microservices/order-service/src/main/java/com/ecommerce/order/client/ProductClient.java`

**Configuration**:
- Circuit breaker name: `productService`
- Fallback class: `ProductClientFallback`

**Fallback Behavior**: Returns placeholder product when Product Service is unavailable:
```java
{
  "id": {requestedId},
  "name": "Product temporarily unavailable",
  "price": 0.00,
  "stockQuantity": 0
}
```

#### 2. Order Service - Order Creation

**File**: `microservices/order-service/src/main/java/com/ecommerce/order/service/OrderService.java`

**Annotation**: `@CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")`

**Fallback**: Throws user-friendly exception

#### 3. Product Service - Product Retrieval

**File**: `microservices/product-service/src/main/java/com/ecommerce/product/service/ProductService.java`

**Annotation**: `@CircuitBreaker(name = "inventoryService", fallbackMethod = "getAllProductsFallback")`

**Fallback**: Returns empty list

#### 4. API Gateway - Route Protection

**File**: `microservices/api-gateway/src/main/resources/application.yml`

**Circuit Breaker Instances**:
- `userService`
- `productService`
- `orderService`
- `paymentService`

### Configuration Parameters

```yaml
resilience4j:
  circuitbreaker:
    instances:
      productService:
        registerHealthIndicator: true
        slidingWindowSize: 10              # Number of calls in window
        minimumNumberOfCalls: 5            # Minimum calls before calculating failure rate
        failureRateThreshold: 50           # Percentage threshold to open circuit
        waitDurationInOpenState: 10000     # Wait time before half-open (ms)
        permittedNumberOfCallsInHalfOpenState: 3  # Test calls in half-open
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

### Circuit Breaker States

1. **CLOSED**: Normal operation, requests flow through
2. **OPEN**: Circuit is open, requests are immediately rejected, fallback executed
3. **HALF_OPEN**: Testing if service recovered, limited requests allowed

### State Transitions

```
CLOSED → (Failure rate > 50% after 5 calls) → OPEN
OPEN → (Wait 10 seconds) → HALF_OPEN
HALF_OPEN → (3 successful calls) → CLOSED
HALF_OPEN → (Any failure) → OPEN
```

### Testing Circuit Breaker

1. Stop Product Service: `pkill -f product-service`
2. Make 5+ calls to Order Service
3. Circuit opens after 50% failure rate
4. Fallback responses return immediately
5. Restart Product Service
6. After 10 seconds, circuit enters HALF_OPEN state
7. Successful calls transition circuit to CLOSED

### Benefits

✅ **Fault Tolerance**: Prevents cascading failures  
✅ **Graceful Degradation**: Fallback responses maintain UX  
✅ **Automatic Recovery**: Self-healing when services recover  
✅ **Performance**: Fast-fail prevents timeout accumulation  
✅ **Resource Protection**: Reduces load on failing services

### Dependencies

**Order Service**:
- `resilience4j-spring-boot3` (v2.1.0)
- `spring-cloud-starter-circuitbreaker-resilience4j`
- `spring-boot-starter-aop`

**Product Service**:
- `resilience4j-spring-boot3` (v2.1.0)
- `spring-boot-starter-aop`

**API Gateway**:
- `resilience4j-spring-boot3` (v2.1.0)

---

## 📊 Distributed Tracing with Zipkin

### Overview

All microservices are configured with **Micrometer Tracing** and **Zipkin** for distributed tracing across the microservices architecture.

### Configuration

Each service has the following configuration in `application.yml`:

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (reduce for production)
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

### How It Works

1. **Trace ID Generation**: Generated at API Gateway when request enters
2. **Propagation**: Trace ID passed through HTTP headers to all services
3. **Span Creation**: Each service call creates a span
4. **Span Relationships**: Parent-child relationships tracked
5. **Span Export**: Spans sent to Zipkin collector
6. **Visualization**: Zipkin UI displays traces

### Accessing Zipkin UI

**URL**: http://localhost:9411

### Features

- **Trace Search**: Search by service name, trace ID, or time range
- **Trace Visualization**: See complete request flow across services
- **Performance Analysis**: Identify bottlenecks and slow services
- **Error Tracking**: See where errors occur in the request chain
- **Dependency Graph**: Visualize service dependencies

### Example Trace Flow

When a user places an order:

```
Frontend → API Gateway (Trace Start)
    ↓
Order Service (Span 1)
    ├──→ Product Service (Span 1.1) [Feign Client]
    ├──→ Database Save (Span 1.2)
    └──→ Kafka Producer (Span 1.3)
            ↓
    ┌───────┼───────┐
    ↓       ↓       ↓
Payment Service  Inventory Service  Notification Service
(Span 2)         (Span 3)           (Span 4)
```

All spans appear in Zipkin with the same Trace ID!

### Logging Pattern

Logs include Trace ID and Span ID:
```
INFO [order-service,abc123def456,span001] Processing order...
INFO [product-service,abc123def456,span002] Fetching product...
```

This allows correlating logs across services using the Trace ID.

### Dependencies

Each service includes:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

---

## 📨 Event-Driven Architecture (Kafka)

### Overview

Apache Kafka is used for asynchronous, event-driven communication between microservices.

### Kafka Setup

**Using Docker Compose**:
```yaml
zookeeper:
  image: confluentinc/cp-zookeeper:latest
  ports:
    - "2181:2181"

kafka:
  image: confluentinc/cp-kafka:latest
  ports:
    - "9092:9092"
  environment:
    KAFKA_BROKER_ID: 1
    KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```

### Kafka Topics

| Topic | Publisher | Consumers | Purpose |
|-------|-----------|-----------|---------|
| `order-created` | Order Service | Payment Service<br>Inventory Service<br>Notification Service | Trigger payment processing, inventory reservation, notifications |

### Event Flow Example

#### Order Creation Event

1. **Publisher (Order Service)**:
```java
@Transactional
public Order createOrder(OrderRequest request) {
    // ... create order ...
    Order savedOrder = orderRepository.save(order);
    
    // Publish event to Kafka
    kafkaTemplate.send("order-created", savedOrder);
    
    return savedOrder;
}
```

2. **Consumer (Payment Service)**:
```java
@KafkaListener(topics = "order-created", groupId = "payment-service")
public void handleOrderCreated(Order order) {
    // Process payment automatically
    Payment payment = new Payment();
    payment.setOrderId(order.getId());
    payment.setAmount(order.getTotalAmount());
    // ... process payment ...
}
```

3. **Consumer (Inventory Service)**:
```java
@KafkaListener(topics = "order-created", groupId = "inventory-service")
public void handleOrderCreated(Order order) {
    // Reserve inventory for each item
    for (OrderItem item : order.getItems()) {
        inventoryService.reserve(item.getProductId(), item.getQuantity());
    }
}
```

4. **Consumer (Notification Service)**:
```java
@KafkaListener(topics = "order-created", groupId = "notification-service")
public void handleOrderCreated(Order order) {
    // Send order confirmation notification
    notificationService.sendOrderConfirmation(order);
}
```

### Kafka Configuration

**Producer Configuration** (Order Service):
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Consumer Configuration** (Payment/Inventory/Notification Services):
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: {service-name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
```

### Benefits

✅ **Loose Coupling**: Services don't need direct references  
✅ **Asynchronous Processing**: Non-blocking operations  
✅ **Scalability**: Multiple consumers can process events  
✅ **Event Sourcing**: Complete event history  
✅ **Reliability**: Kafka ensures message delivery

---

## 💻 Frontend Application

### Overview

React.js frontend application with Material-UI components.

### Technology Stack

- **React 18**: UI library
- **React Router**: Client-side routing
- **Material-UI (MUI)**: Component library
- **Axios**: HTTP client
- **Context API**: State management

### Project Structure

```
microservices/ecommerce-ui/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   └── Navbar.js
│   ├── pages/
│   │   ├── Home.js
│   │   ├── Products.js
│   │   ├── ProductDetail.js
│   │   ├── Cart.js
│   │   ├── Login.js
│   │   ├── Register.js
│   │   └── Orders.js
│   ├── context/
│   │   ├── AuthContext.js
│   │   └── CartContext.js
│   ├── services/
│   │   └── api.js
│   ├── App.js
│   ├── index.js
│   └── index.css
└── package.json
```

### Key Features

1. **User Authentication**
   - Login with username/email
   - User registration
   - JWT token storage in localStorage
   - Protected routes

2. **Product Browsing**
   - Product listing
   - Product search
   - Category filtering
   - Product details view

3. **Shopping Cart**
   - Add/remove items
   - Quantity management
   - Cart persistence (localStorage)
   - Total calculation

4. **Order Management**
   - Place orders
   - View order history
   - Order status tracking

### API Configuration

**File**: `microservices/ecommerce-ui/src/services/api.js`

```javascript
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});
```

### Authentication Context

**File**: `microservices/ecommerce-ui/src/context/AuthContext.js`

- Manages user authentication state
- Provides login/logout functions
- Stores JWT token
- Handles API authentication headers

### Cart Context

**File**: `microservices/ecommerce-ui/src/context/CartContext.js`

- Manages shopping cart state
- Provides add/remove/update functions
- Persists cart to localStorage

### Running the Frontend

```bash
cd microservices/ecommerce-ui
npm install  # First time only
npm start    # Starts on http://localhost:3000
```

### Environment Variables

Create `.env` file (optional):
```
REACT_APP_API_URL=http://localhost:8080
```

---

## 🌱 Database Seeding

### Overview

The project includes scripts to seed the database with dummy data for testing.

### Prerequisites

1. **All services must be running** (tables must exist)
2. **PostgreSQL must be running**
3. **Tables created** (services create them on startup)

### Option 1: SQL Scripts (Recommended)

#### Seed Users

**File**: `microservices/seed-users.sql`

```bash
cd microservices
psql -h localhost -U ujjawalkumar -d postgres -f seed-users.sql
```

**Creates**: 10,000 dummy users with:
- Username: `user1` to `user10000`
- Email: `user1@example.com` to `user10000@example.com`
- Password: `password` (BCrypt hashed)
- Random first/last names

#### Seed Products

**File**: `microservices/seed-products.sql`

```bash
cd microservices
psql -h localhost -U ujjawalkumar -d postgres -f seed-products.sql
```

**Creates**: 10,000 dummy products with:
- Various product names
- 15 different categories
- Random prices ($10-$1000)
- Random stock quantities (0-100)
- Corresponding inventory records

### Option 2: Combined Script

**File**: `microservices/seed-data.sql`

Contains both users and products. Execute:

```bash
cd microservices
psql -h localhost -U ujjawalkumar -d postgres -f seed-data.sql
```

### Verify Seeded Data

```sql
-- Check user count
SELECT COUNT(*) FROM users;

-- Check product count
SELECT COUNT(*) FROM products;

-- Check inventory count
SELECT COUNT(*) FROM inventory;

-- View sample users
SELECT id, username, email FROM users LIMIT 10;

-- View sample products
SELECT id, name, price, category FROM products LIMIT 10;
```

### Login Credentials (Seeded Users)

**Default Credentials**:
- Username: `user1` or `testuser`
- Password: `password`

**Note**: All seeded users have password `password` (BCrypt hashed).

### Troubleshooting Seeding

**Issue**: "relation 'users' does not exist"
- **Solution**: Start User Service first to create tables

**Issue**: "relation 'products' does not exist"
- **Solution**: Start Product Service first to create tables

**Issue**: "permission denied"
- **Solution**: Ensure PostgreSQL user has proper permissions

---

## 🧪 Testing

### Backend Testing

#### Unit Tests

```bash
# Test specific service
cd microservices/user-service
mvn test

# Test all services
cd microservices
for dir in */; do
  cd "$dir"
  mvn test
  cd ..
done
```

#### Integration Tests

Test API endpoints using curl or Postman:

```bash
# Test User Registration
curl -X POST http://localhost:8080/api/users/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'

# Test Login
curl -X POST http://localhost:8080/api/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# Test Get Products
curl http://localhost:8080/api/products

# Test Create Order (requires JWT token)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "items": [{"productId": 1, "quantity": 2}],
    "shippingAddress": "123 Main St"
  }'
```

### Frontend Testing

```bash
cd microservices/ecommerce-ui
npm test
```

### Manual Testing Checklist

- [ ] User registration
- [ ] User login
- [ ] Browse products
- [ ] Search products
- [ ] Add to c