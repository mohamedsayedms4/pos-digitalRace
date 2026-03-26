package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Purchase Invoice Data Transfer Object")
public class PurchaseInvoiceDto {
    private Long id;
    private String invoiceNumber;
    private LocalDateTime invoiceDate;
    private Long supplierId;
    private String supplierName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private String status;
    private List<PurchaseInvoiceItemDto> items;
}
