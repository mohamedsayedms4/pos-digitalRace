package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Supplier Data Transfer Object")
public class SupplierDto {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String email;
    private BigDecimal balance;
    private String taxNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
