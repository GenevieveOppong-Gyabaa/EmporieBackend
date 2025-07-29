# **Emporio E-commerce Backend Documentation**

## **📋 Table of Contents**
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture](#architecture)
4. [Database Schema](#database-schema)
5. [API Endpoints](#api-endpoints)
6. [Security](#security)
7. [Features](#features)
8. [Configuration](#configuration)
9. [Deployment](#deployment)
10. [Development Guide](#development-guide)

---

## **🏗️ Project Overview**

**Emporio** is a comprehensive e-commerce backend built with Spring Boot that provides a complete marketplace solution with features including user management, product catalog, shopping cart, orders, reviews, real-time chat, and admin functionality.

### **Key Features**
- ✅ User authentication with JWT
- ✅ Product management with categories
- ✅ Shopping cart functionality
- ✅ Order processing
- ✅ Review and rating system
- ✅ Real-time chat messaging
- ✅ Admin dashboard
- ✅ Email notifications
- ✅ Image upload support
- ✅ Role-based access control

---

## **🛠️ Technology Stack**

### **Core Framework**
- **Spring Boot 3.5.3** - Main application framework
- **Java 17** - Programming language
- **Maven** - Build tool and dependency management

### **Database & Persistence**
- **PostgreSQL** - Primary database
- **Spring Data JPA** - ORM framework
- **Hibernate** - JPA implementation

### **Security**
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing

### **Real-time Communication**
- **WebSocket** - Real-time chat and notifications
- **STOMP** - Messaging protocol

### **Additional Libraries**
- **Spring Mail** - Email notifications
- **SpringDoc OpenAPI** - API documentation
- **Multipart File Upload** - Image handling

---

## **🏛️ Architecture**

### **Layered Architecture**
```
┌─────────────────────────────────────┐
│           Controllers               │  ← REST API Layer
├─────────────────────────────────────┤
│            Services                 │  ← Business Logic Layer
├─────────────────────────────────────┤
│          Repositories              │  ← Data Access Layer
├─────────────────────────────────────┤
│           Database                 │  ← Data Storage Layer
└─────────────────────────────────────┘
```

### **Package Structure**
```
com.mobiledev.emporio/
├── config/           # Configuration classes
├── controller/       # REST API controllers
├── dto/             # Data Transfer Objects
├── exceptions/       # Custom exceptions
├── model/           # Entity classes
├── repositories/    # Data access interfaces
├── security/        # Security configuration
└── services/        # Business logic services
```

---

## **🗄️ Database Schema**

### **Core Entities**

#### **User Entity**
```java
@Entity
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Role role; // BUYER, SELLER, ADMIN
    private Cart cart;
}
```

#### **Product Entity**
```java
@Entity
public class Product {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Double discountPrice;
    private Boolean onDeal;
    private User seller;
    private Category category;
    private List<String> imageUrls;
    private List<String> tags;
    private int views;
}
```

#### **Order Entity**
```java
@Entity
public class Order {
    private Long id;
    private User buyer;
    private User seller;
    private List<Product> products;
    private String status;
    private LocalDateTime orderDate;
    private String paymentStatus;
    private List<OrderItem> items;
    private String shippingAddress;
    private String paymentMethod;
    private Double total;
}
```

#### **Cart Entity**
```java
@Entity
public class Cart {
    private Long id;
    private User user;
    private List<CartItem> items;
    private Double total;
}
```

### **Supporting Entities**
- **Category** - Product categorization
- **Review** - Product reviews and ratings
- **ChatMessage** - Real-time messaging
- **Notification** - User notifications
- **OrderItem** - Individual items in orders

---

## **🔌 API Endpoints**

### **Authentication (`/auth`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | Register new user |
| `POST` | `/auth/login` | User login |
| `POST` | `/auth/register-admin` | Register admin user |
| `POST` | `/auth/become-seller` | Promote user to seller |
| `POST` | `/auth/promote-to-seller` | Alternative seller promotion |
| `POST` | `/auth/refresh-token` | Refresh JWT token |

### **Products (`/products`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/products/deals` | Get products on sale |
| `GET` | `/products/by-category/{id}` | Get products by category |
| `GET` | `/products/by-tag` | Get products by tag |
| `GET` | `/products/{id}` | Get product details |
| `POST` | `/products` | Create new product |
| `PUT` | `/products/{id}` | Update product |
| `DELETE` | `/products/{id}` | Delete product |
| `POST` | `/products/{id}/upload-images` | Upload product images |

### **Categories (`/categories`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/categories` | Get all categories |
| `GET` | `/categories/{id}` | Get category by ID |
| `POST` | `/categories` | Create category (admin only) |

### **Orders (`/api/orders`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/orders` | Get current user's orders |
| `GET` | `/api/orders/buyer/{username}` | Get user's orders |
| `GET` | `/api/orders/seller/{username}` | Get seller's orders |
| `POST` | `/api/orders` | Place new order |
| `PUT` | `/api/orders/{id}/status` | Update order status |
| `DELETE` | `/api/orders/{id}/cancel` | Cancel order |

### **Cart (`/cart`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/cart` | Get user's cart |
| `POST` | `/cart/items` | Add item to cart |
| `PUT` | `/cart/items/{id}` | Update cart item |
| `DELETE` | `/cart/items/{id}` | Remove cart item |

### **Admin (`/admin`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/admin/users` | List all users |
| `POST` | `/admin/users/{id}/promote` | Promote user to seller |
| `POST` | `/admin/users/{id}/demote` | Demote user to buyer |
| `POST` | `/admin/users/{id}/ban` | Ban user |
| `POST` | `/admin/users/{id}/unban` | Unban user |
| `GET` | `/admin/products` | List all products |
| `DELETE` | `/admin/products/{id}` | Remove product |
| `GET` | `/admin/orders` | List all orders |
| `PUT` | `/admin/orders/{id}/mark-paid` | Mark order as paid |

### **Reviews (`/reviews`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/reviews/product/{id}` | Get product reviews |
| `POST` | `/reviews` | Create review |
| `GET` | `/reviews/pending` | Get pending reviews (admin) |
| `POST` | `/reviews/{id}/approve` | Approve review (admin) |
| `POST` | `/reviews/{id}/reject` | Reject review (admin) |

### **Chat (`/chat`)**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/chat/messages` | Get chat messages |
| `POST` | `/chat/messages` | Send message |
| `GET` | `/chat/conversations` | Get conversations |

### **Health Check**
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Application health check |
| `GET` | `/health` | Detailed health status |

---

## **🔐 Security**

### **Authentication**
- **JWT-based authentication** for stateless sessions
- **Role-based access control** (BUYER, SELLER, ADMIN)
- **Password encryption** using BCrypt
- **Token refresh** mechanism

### **Authorization Levels**
```java
public enum Role {
    BUYER,   // Can browse, add to cart, place orders
    SELLER,  // Can create/manage products, view orders
    ADMIN    // Full system access, user management
}
```

### **Security Configuration**
- **CORS enabled** for cross-origin requests
- **CSRF disabled** for API usage
- **Rate limiting** (10 requests per minute per IP)
- **Public endpoints**: `/auth/**`, `/products/**`, `/categories/**`
- **Protected endpoints**: Require authentication

### **JWT Token Structure**
```json
{
  "sub": "user@example.com",
  "iat": 1753638132,
  "exp": 1753641732
}
```

---

## **✨ Features**

### **User Management**
- ✅ User registration and login
- ✅ Role-based access (BUYER, SELLER, ADMIN)
- ✅ Automatic seller promotion
- ✅ Password reset functionality
- ✅ User profile management

### **Product Management**
- ✅ Product creation and management
- ✅ Category-based organization
- ✅ Image upload support (up to 2MB per image)
- ✅ Price formatting in Ghana Cedis (₵)
- ✅ Product search and filtering
- ✅ Deal/discount management

### **Shopping Experience**
- ✅ Shopping cart functionality
- ✅ Add/remove/update cart items
- ✅ Order placement and tracking
- ✅ Payment status management
- ✅ Order history

### **Review System**
- ✅ Product reviews and ratings
- ✅ Review moderation (admin approval)
- ✅ Average rating calculation
- ✅ Review count tracking

### **Real-time Features**
- ✅ WebSocket-based chat system
- ✅ Real-time notifications
- ✅ Live order updates

### **Admin Features**
- ✅ User management (promote, demote, ban)
- ✅ Product moderation
- ✅ Order management
- ✅ Review approval system
- ✅ System monitoring

### **Communication**
- ✅ Email notifications
- ✅ Password reset emails
- ✅ Order confirmation emails

---

## **⚙️ Configuration**

### **Database Configuration**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mobile_app
spring.datasource.username=postgres
spring.datasource.password=pos@s_areaL1
spring.jpa.hibernate.ddl-auto=update
```

### **File Upload Configuration**
```properties
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
upload.path=uploads/images
```

### **Email Configuration**
```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=f21eea87fbfdbd
spring.mail.password=c07ab7d03a60e7
```

### **Server Configuration**
```properties
server.address=0.0.0.0
server.port=8080
spring.application.name=emporio
```

---

## **📦 Deployment**

### **Prerequisites**
- Java 17 or higher
- PostgreSQL database
- Maven 3.6+

### **Build Process**
```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run
```

### **Database Setup**
1. Create PostgreSQL database: `mobile_app`
2. Update `application.properties` with your database credentials
3. Application will auto-create tables on startup

### **Environment Variables**
- `DATABASE_URL` - PostgreSQL connection string
- `JWT_SECRET` - JWT signing secret
- `MAIL_HOST` - SMTP server host
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password

### **Production Considerations**
- ✅ Use environment variables for sensitive data
- ✅ Configure proper CORS origins
- ✅ Set up SSL/TLS
- ✅ Configure proper logging
- ✅ Set up monitoring and health checks

---

## **🛠️ Development Guide**

### **Running Locally**
1. **Start PostgreSQL**
2. **Update database credentials** in `application.properties`
3. **Run the application**: `mvn spring-boot:run`
4. **Access API**: `http://localhost:8080`

### **API Testing**
- **Health Check**: `GET http://localhost:8080/`
- **Categories**: `GET http://localhost:8080/categories`
- **Products**: `GET http://localhost:8080/products`

### **Database Initialization**
- Categories are automatically created on startup via `CategoryInitializer`
- Default categories: Electronics, Fashion, Home, Beauty, Health, Toys, Groceries, Books, Sports, Other

### **Key Components**

#### **Controllers**
- `AuthController` - User authentication and registration
- `ProductController` - Product management
- `OrderController` - Order processing
- `CartController` - Shopping cart operations
- `AdminController` - Admin functionality
- `ReviewController` - Review system
- `ChatController` - Real-time messaging

#### **Services**
- `UserService` - User management logic
- `ProductService` - Product business logic
- `OrderService` - Order processing
- `CartService` - Cart operations
- `ReviewService` - Review management
- `ChatService` - Messaging logic
- `NotificationService` - Email notifications

#### **Models**
- `User` - User entity with roles
- `Product` - Product with categories and images
- `Order` - Order with items and status
- `Cart` - Shopping cart with items
- `Category` - Product categories
- `Review` - Product reviews
- `ChatMessage` - Real-time messages

---

## **📊 API Response Examples**

### **User Registration**
```json
{
  "id": 123,
  "email": "user@example.com",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### **Product Creation**
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone model",
  "price": 999.99,
  "stock": 10,
  "seller": {
    "id": 123,
    "username": "seller@example.com"
  },
  "category": {
    "id": 1,
    "name": "Electronics"
  }
}
```

### **Category Products**
```json
[
  {
    "id": 1,
    "name": "iPhone 15",
    "price": "₵999.99",
    "discountPrice": "₵899.99",
    "description": "Latest iPhone model",
    "inStock": true,
    "image": "/uploads/iphone.jpg"
  }
]
```

### **Order Response**
```json
{
  "id": 1,
  "title": "Order #12345",
  "date": "2024-01-15T10:30:00",
  "status": "PENDING",
  "total": "₵299.99"
}
```

---

## **🔧 Troubleshooting**

### **Common Issues**

#### **Database Connection**
- Ensure PostgreSQL is running
- Check database credentials in `application.properties`
- Verify database `mobile_app` exists

#### **JWT Authentication**
- Check JWT secret configuration
- Verify token expiration settings
- Ensure proper Authorization header format

#### **File Upload**
- Check file size limits (2MB max)
- Verify upload directory permissions
- Ensure proper Content-Type headers

#### **CORS Issues**
- Verify CORS configuration in `SecurityConfig`
- Check allowed origins and methods
- Ensure proper preflight request handling

### **Logs and Debugging**
- Enable SQL logging: `spring.jpa.show-sql=true`
- Check application logs for errors
- Use health endpoint: `GET /health`

---

## **📈 Performance Considerations**

### **Database Optimization**
- Use appropriate indexes on frequently queried fields
- Implement pagination for large datasets
- Consider caching for static data

### **API Optimization**
- Implement response compression
- Use DTOs to limit data transfer
- Consider caching for product listings

### **Security Best Practices**
- Regularly rotate JWT secrets
- Implement rate limiting
- Use HTTPS in production
- Validate all input data

---

## **🤝 Contributing**

### **Code Style**
- Follow Java naming conventions
- Use meaningful variable and method names
- Add proper JavaDoc comments
- Follow Spring Boot best practices

### **Testing**
- Write unit tests for services
- Add integration tests for controllers
- Test security configurations
- Verify API responses

---

## **📝 Summary**

**Emporio** is a feature-rich e-commerce backend that provides:

- **Complete marketplace functionality** with user, product, and order management
- **Real-time communication** through WebSocket chat and notifications
- **Robust security** with JWT authentication and role-based access
- **Scalable architecture** following Spring Boot best practices
- **Comprehensive API** covering all e-commerce operations
- **Admin dashboard** for system management and moderation

The system is designed to handle a full e-commerce marketplace with support for multiple sellers, buyers, and administrators, making it suitable for both small and large-scale e-commerce operations.

---

**🎯 This documentation covers the complete backend architecture and functionality. For frontend integration, refer to the API endpoints section above.**

---

*Last Updated: January 2024*
*Version: 1.0.0* 