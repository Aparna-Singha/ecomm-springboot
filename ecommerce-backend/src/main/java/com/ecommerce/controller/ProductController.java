package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", createdProduct));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(@PathVariable String category) {
        List<ProductDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@RequestParam String name) {
        List<ProductDTO> products = productService.searchProducts(name);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }
}
