package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request object for creating or updating a product")
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Schema(description = "Product name", example = "Wireless Mouse")
    private String name;

    @Schema(description = "Product description", example = "Ergonomic wireless mouse with 2.4GHz receiver")
    private String description;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    @Schema(description = "Product purchase price", example = "20.00")
    private BigDecimal purchasePrice;

    @NotNull(message = "Sale price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sale price must be greater than zero")
    @Schema(description = "Product sale price", example = "25.50")
    private BigDecimal salePrice;

    @Schema(description = "Product code (Barcode/QR)", example = "PRD-12345")
    private String productCode;

    @Min(value = 0, message = "Stock cannot be negative")
    @Schema(description = "Initial stock quantity", example = "100")
    private Integer stock;

    @Schema(description = "ID of the category this product belongs to", example = "3")
    private Long categoryId;
}
