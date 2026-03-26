package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPlanRequest {
    
    @NotNull(message = "Purchase Invoice ID is required")
    private Long purchaseInvoiceId;
    
    @NotNull(message = "Number of months is required")
    @Min(value = 1, message = "At least 1 month is required")
    private Integer numberOfMonths;
    
    private BigDecimal downPayment; // Optional down payment amount
    
    @NotNull(message = "Start date for installments is required")
    private LocalDate startDate;
}
