package com.example.pos.service;

import com.example.pos.dto.*;
import com.example.pos.entity.*;
import com.example.pos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierTransactionRepository transactionRepository;
    private final AuditService auditService;
    private final ProductService productService; // To reuse find logic

    @Transactional
    public PurchaseInvoiceDto createPurchaseInvoice(PurchaseInvoiceRequest request, Locale locale) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        BigDecimal totalAmount = BigDecimal.ZERO;
        
        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .invoiceNumber("PUR-" + System.currentTimeMillis())
                .invoiceDate(request.getInvoiceDate())
                .supplier(supplier)
                .paidAmount(request.getPaidAmount())
                .build();

        for (PurchaseItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));
            
            BigDecimal itemTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            PurchaseInvoiceItem item = PurchaseInvoiceItem.builder()
                    .invoice(invoice)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .totalPrice(itemTotal)
                    .build();
            
            invoice.getItems().add(item);

            // Update product stock
            product.setStock(product.getStock() + itemReq.getQuantity());
            // Update purchase price for the product if needed (Last Purchase Price)
            product.setPurchasePrice(itemReq.getUnitPrice());
            productRepository.save(product);
        }

        invoice.setTotalAmount(totalAmount);
        BigDecimal remaining = totalAmount.subtract(request.getPaidAmount());
        invoice.setRemainingAmount(remaining);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(PurchaseInvoice.InvoiceStatus.PAID);
        } else if (request.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(PurchaseInvoice.InvoiceStatus.PARTIAL);
        } else {
            invoice.setStatus(PurchaseInvoice.InvoiceStatus.UNPAID);
        }

        purchaseInvoiceRepository.save(invoice);

        // Update supplier balance (debt)
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            supplier.setBalance(supplier.getBalance().add(remaining));
            supplierRepository.save(supplier);

            // Record transaction
            SupplierTransaction transaction = SupplierTransaction.builder()
                    .supplier(supplier)
                    .amount(remaining)
                    .type(SupplierTransaction.TransactionType.PURCHASE_INVOICE)
                    .transactionDate(invoice.getInvoiceDate())
                    .description("Remaining amount from invoice " + invoice.getInvoiceNumber())
                    .balanceAfter(supplier.getBalance())
                    .build();
            transactionRepository.save(transaction);
        }

        auditService.logAction("PURCHASE_CREATE", "PURCHASE_INVOICE", invoice.getId(), 
                "Recorded purchase invoice " + invoice.getInvoiceNumber() + " from supplier " + supplier.getName());

        return mapToDto(invoice);
    }

    private PurchaseInvoiceDto mapToDto(PurchaseInvoice invoice) {
        return PurchaseInvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .supplierId(invoice.getSupplier().getId())
                .supplierName(invoice.getSupplier().getName())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .status(invoice.getStatus().name())
                .items(invoice.getItems().stream().map(item -> PurchaseInvoiceItemDto.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
