package com.example.pos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierTransaction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private LocalDateTime transactionDate;

    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    public enum TransactionType {
        PURCHASE_INVOICE, PAYMENT, REFUND
    }
}
