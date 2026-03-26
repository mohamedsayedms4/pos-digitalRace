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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing products (Requires PRODUCT_* authorities)")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Get all products", description = "Retrieves a list of all products in the system.")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.<List<ProductDto>>builder()
                .success(true)
                .data(productService.getAllProducts())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Get a single product", description = "Retrieves details of a specific product by ID.")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<ProductDto>builder()
                .success(true)
                .data(productService.getProductById(id, locale))
                .build());
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Create a new product", description = "Adds a new product with multiple images.")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) org.springframework.web.multipart.MultipartFile[] images) {
        ProductDto response = productService.createProduct(request, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductDto>builder()
                        .success(true)
                        .data(response)
                        .build());
    }

    @PutMapping(value = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    @Operation(summary = "Update an existing product", description = "Updates details of an existing product including images.")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) org.springframework.web.multipart.MultipartFile[] images,
            Locale locale) {
        ProductDto response = productService.updateProduct(id, request, images, locale);
        return ResponseEntity.ok(ApiResponse.<ProductDto>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/images/{fileName:.+}")
    @Operation(summary = "Serve product image", description = "Retrieves a product image file.")
    public ResponseEntity<org.springframework.core.io.Resource> getProductImage(@PathVariable String fileName) {
        try {
            java.nio.file.Path filePath = ((com.example.pos.service.FileStorageService)
                    org.springframework.web.context.support.WebApplicationContextUtils
                    .getRequiredWebApplicationContext(
                            ((org.springframework.web.context.request.ServletRequestAttributes) 
                                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                                    .getRequest().getServletContext())
                    .getBean(com.example.pos.service.FileStorageService.class))
                    .getFilePath(fileName);
            
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.IMAGE_JPEG) // Simple default, can be dynamic
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/barcode")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Get product barcode", description = "Generates a Base64 encoded barcode image for the product.")
    public ResponseEntity<ApiResponse<String>> getProductBarcode(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .data(productService.getProductBarcode(id, locale))
                .build());
    }

    @GetMapping("/{id}/qrcode")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Get product QR code", description = "Generates a Base64 encoded QR code image for the product.")
    public ResponseEntity<ApiResponse<String>> getProductQrCode(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .data(productService.getProductQrCode(id, locale))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    @Operation(summary = "Delete a product", description = "Removes a product from the system by ID.")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id, Locale locale) {
        productService.deleteProduct(id, locale);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }
}
