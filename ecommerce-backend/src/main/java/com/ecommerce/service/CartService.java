package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, UserService userService, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.productService = productService;
    }

    @Transactional
    public CartItemDTO addToCart(AddToCartRequest request) {
        User user = userService.getUserEntityById(request.getUserId());
        Product product = productService.getProductEntityById(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndProduct(user, product);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        }

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return mapToDTO(savedCartItem);
    }

    public CartResponse getCart(Long userId) {
        userService.getUserEntityById(userId);

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<CartItemDTO> items = cartItems.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();

        CartResponse response = new CartResponse();
        response.setUserId(userId);
        response.setItems(items);
        response.setTotalAmount(totalAmount);
        response.setTotalItems(totalItems);

        return response;
    }

    @Transactional
    public void clearCart(Long userId) {
        userService.getUserEntityById(userId);
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional
    public CartItemDTO updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        cartItem.setQuantity(quantity);
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        return mapToDTO(updatedCartItem);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    public List<CartItem> getCartItemEntities(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    private CartItemDTO mapToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setUserId(cartItem.getUser().getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setSubtotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        return dto;
    }
}
