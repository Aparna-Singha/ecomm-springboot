package com.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String shippingAddress;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long userId, String shippingAddress) {
        this.userId = userId;
        this.shippingAddress = shippingAddress;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
