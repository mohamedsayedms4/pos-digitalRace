package com.example.pos.controller;

import com.example.pos.dto.ApiResponse;
import com.example.pos.dto.ProductDto;
import com.example.pos.dto.ProductRequest;
import com.example.pos.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.<List<ProductDto>>builder()
                .success(true)
                .data(productService.getAllProducts())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<ProductDto>builder()
                .success(true)
                .data(productService.getProductById(id, locale))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductDto response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductDto>builder()
                        .success(true)
                        .data(response)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Locale locale) {
        ProductDto response = productService.updateProduct(id, request, locale);
        return ResponseEntity.ok(ApiResponse.<ProductDto>builder()
                .success(true)
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id, Locale locale) {
        productService.deleteProduct(id, locale);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }
}
