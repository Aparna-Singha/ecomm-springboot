package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartItemDTO cartItem = cartService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart successfully", cartItem));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }

    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        CartItemDTO updatedItem = cartService.updateCartItemQuantity(userId, productId, quantity);
        if (updatedItem == null) {
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", updatedItem));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }
}
