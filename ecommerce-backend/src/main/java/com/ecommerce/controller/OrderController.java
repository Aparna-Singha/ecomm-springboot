package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(@PathVariable Long orderId) {
        OrderDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
