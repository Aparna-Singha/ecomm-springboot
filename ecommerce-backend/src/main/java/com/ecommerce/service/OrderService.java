package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CartService cartService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, UserService userService, CartService cartService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        User user = userService.getUserEntityById(request.getUserId());
        List<CartItem> cartItems = cartService.getCartItemEntities(request.getUserId());

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty. Add items to cart before placing an order.");
        }

        // Validate stock availability
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStock() +
                                ", Requested: " + cartItem.getQuantity());
            }
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress() != null ?
                request.getShippingAddress() : user.getAddress());

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            BigDecimal subtotal = cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setSubtotal(subtotal);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);

            // Deduct stock
            productService.updateStock(cartItem.getProduct().getId(), cartItem.getQuantity());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cartService.clearCart(request.getUserId());

        return mapToDTO(savedOrder);
    }

    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToDTO(order);
    }

    public Order getOrderEntityById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    public List<OrderDTO> getOrdersByUserId(Long userId) {
        userService.getUserEntityById(userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return mapToDTO(updatedOrder);
    }

    @Transactional
    public OrderDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
                order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel order that has been shipped or delivered");
        }

        // Restore stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        return mapToDTO(updatedOrder);
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getName());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemDTO> items = order.getOrderItems().stream()
                .map(this::mapOrderItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(items);

        if (order.getPayment() != null) {
            dto.setPayment(mapPaymentToDTO(order.getPayment()));
        }

        return dto;
    }

    private OrderItemDTO mapOrderItemToDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        return dto;
    }

    private PaymentDTO mapPaymentToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setRazorpayOrderId(payment.getRazorpayOrderId());
        dto.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus().name());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }
}
