package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for creating or updating a supplier")
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Schema(description = "Supplier name", example = "Global Electronics Ltd")
    private String name;

    @Schema(description = "Phone number", example = "+201234567890")
    private String phone;

    @Schema(description = "Office address", example = "Cairo, Egypt")
    private String address;

    @Schema(description = "Email address", example = "contact@global-el.com")
    private String email;

    @Schema(description = "Tax identification number", example = "TAX-998877")
    private String taxNumber;
}
