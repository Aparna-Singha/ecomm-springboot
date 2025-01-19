# E-Commerce Backend API

A minimal e-commerce backend system built with Spring Boot that includes product listings, shopping carts, order placement, payment processing with Razorpay integration, and order status updates via webhooks.

## Features

- **Product Management**: Create and list products
- **Shopping Cart**: Add items, view cart, clear cart
- **Order Processing**: Create orders from cart, view order details
- **Payment Integration**: Razorpay payment gateway integration
- **Webhook Support**: Payment status updates via webhooks
- **Mock Payment Mode**: Testing without actual Razorpay transactions

## Tech Stack

- Java 17
- Spring Boot 3.4.3
- Spring Data JPA
- H2 Database (in-memory)
- Razorpay Java SDK
- Lombok
- Maven

## Database Schema

### Entities
- **User**: id, name, email, phone, address, created_at
- **Product**: id, name, description, price, stock, category, image_url, created_at, updated_at
- **CartItem**: id, user_id, product_id, quantity, created_at
- **Order**: id, user_id, total_amount, status, shipping_address, created_at, updated_at
- **OrderItem**: id, order_id, product_id, quantity, unit_price, subtotal
- **Payment**: id, order_id, razorpay_order_id, razorpay_payment_id, razorpay_signature, amount, currency, status, payment_method, created_at, updated_at

### Relationships
- User → Orders (One-to-Many)
- User → CartItems (One-to-Many)
- Product → CartItems (One-to-Many)
- Order → OrderItems (One-to-Many)
- Order → Payment (One-to-One)

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. Clone or extract the project:
```bash
cd ecommerce-backend
```

2. Build the project:
```bash
./mvnw clean install
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

The server will start at `http://localhost:8080`

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Razorpay Configuration (Replace with your credentials)
razorpay.key.id=your_razorpay_key_id
razorpay.key.secret=your_razorpay_key_secret

# Mock Payment Mode (set to true for testing)
payment.mock.enabled=false
```

### H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:ecommercedb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Product APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create a new product |
| GET | `/api/products` | Get all products |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/category/{category}` | Get products by category |
| GET | `/api/products/search?name={name}` | Search products by name |

### Cart APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cart/add` | Add item to cart |
| GET | `/api/cart/{userId}` | Get user's cart |
| DELETE | `/api/cart/{userId}/clear` | Clear user's cart |
| PUT | `/api/cart/{userId}/items/{productId}?quantity={qty}` | Update cart item quantity |
| DELETE | `/api/cart/{userId}/items/{productId}` | Remove item from cart |

### Order APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create order from cart |
| GET | `/api/orders/{orderId}` | Get order details |
| GET | `/api/orders/user/{userId}` | Get user's order history |
| PUT | `/api/orders/{orderId}/status?status={status}` | Update order status |
| POST | `/api/orders/{orderId}/cancel` | Cancel order |

### Payment APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/create` | Create payment for order |
| GET | `/api/payments/order/{orderId}` | Get payment by order ID |
| GET | `/api/payments/razorpay-key` | Get Razorpay public key |

### Webhook APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/webhooks/payment` | Handle payment callback |

### User APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create a new user |
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |

## Sample API Requests

### Create Product
```json
POST /api/products
{
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 799.00,
    "stock": 100,
    "category": "Electronics"
}
```

### Add to Cart
```json
POST /api/cart/add
{
    "userId": 1,
    "productId": 1,
    "quantity": 2
}
```

### Create Order
```json
POST /api/orders
{
    "userId": 1,
    "shippingAddress": "123 Main St, Mumbai"
}
```

### Create Payment
```json
POST /api/payments/create
{
    "orderId": 1
}
```

### Payment Webhook Callback
```json
POST /api/webhooks/payment
{
    "razorpay_order_id": "order_xxx",
    "razorpay_payment_id": "pay_xxx",
    "razorpay_signature": "signature_xxx"
}
```

## Order Statuses

- `PENDING` - Order created, awaiting payment
- `PAYMENT_PENDING` - Payment initiated
- `PAID` - Payment successful
- `PROCESSING` - Order being processed
- `SHIPPED` - Order shipped
- `DELIVERED` - Order delivered
- `CANCELLED` - Order cancelled

## Payment Statuses

- `PENDING` - Payment not yet initiated
- `CREATED` - Razorpay order created
- `SUCCESS` - Payment successful
- `FAILED` - Payment failed
- `REFUNDED` - Payment refunded

## Testing with Mock Payment

1. Set `payment.mock.enabled=true` in application.properties
2. Create an order and initiate payment
3. The system will automatically trigger a mock webhook after 3 seconds
4. Order status will be updated to PAID

## Sample Data

The application automatically creates sample data on startup:
- 2 sample users
- 5 sample products

## Postman Collection

Import the `postman_collection.json` file into Postman for ready-to-use API requests.

## Project Structure

```
src/main/java/com/ecommerce/
├── EcommerceApplication.java
├── config/
│   └── DataInitializer.java
├── controller/
│   ├── CartController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ProductController.java
│   ├── UserController.java
│   └── WebhookController.java
├── dto/
│   ├── AddToCartRequest.java
│   ├── ApiResponse.java
│   ├── CartItemDTO.java
│   ├── CartResponse.java
│   ├── CreateOrderRequest.java
│   ├── CreatePaymentRequest.java
│   ├── OrderDTO.java
│   ├── OrderItemDTO.java
│   ├── PaymentCallbackRequest.java
│   ├── PaymentDTO.java
│   ├── ProductDTO.java
│   └── UserDTO.java
├── exception/
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java
│   ├── PaymentException.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Product.java
│   └── User.java
├── repository/
│   ├── CartItemRepository.java
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── ProductRepository.java
│   └── UserRepository.java
└── service/
    ├── CartService.java
    ├── OrderService.java
    ├── PaymentService.java
    ├── ProductService.java
    └── UserService.java
```

## License

This project is created for educational purposes.
