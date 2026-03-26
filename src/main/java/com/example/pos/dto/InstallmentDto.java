package com.example.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDto {
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private String status;
}
