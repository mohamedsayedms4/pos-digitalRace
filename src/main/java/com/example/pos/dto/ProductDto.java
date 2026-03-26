package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Product Data Transfer Object")
public class ProductDto {
    @Schema(description = "Product ID", example = "10")
    private Long id;
    
    @Schema(description = "Product name", example = "iPhone 15 Pro")
    private String name;
    
    @Schema(description = "Product description", example = "Latest Apple smartphone with A17 Pro chip")
    private String description;
    
    @Schema(description = "Product purchase price", example = "20.00")
    private BigDecimal purchasePrice;

    @Schema(description = "Product sale price", example = "999.99")
    private BigDecimal salePrice;

    @Schema(description = "Product code (Barcode/QR)", example = "PRD-12345")
    private String productCode;
    
    @Schema(description = "Current stock quantity", example = "50")
    private Integer stock;

    @Schema(description = "List of product image URLs")
    private java.util.List<String> imageUrls;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "ID of the product's category", example = "2")
    private Long categoryId;
    
    @Schema(description = "Name of the product's category", example = "Mobile Phones")
    private String categoryName;
}
