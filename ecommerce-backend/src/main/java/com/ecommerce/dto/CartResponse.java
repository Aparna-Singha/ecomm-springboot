package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private Long userId;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private Integer totalItems;

    public CartResponse() {
    }

    public CartResponse(Long userId, List<CartItemDTO> items, BigDecimal totalAmount, Integer totalItems) {
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.totalItems = totalItems;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}
