package com.example.pos.service;

import com.example.pos.dto.InstallmentDto;
import com.example.pos.dto.InstallmentPlanRequest;
import com.example.pos.entity.Installment;
import com.example.pos.entity.PurchaseInvoice;
import com.example.pos.entity.Supplier;
import com.example.pos.entity.SupplierTransaction;
import com.example.pos.repository.InstallmentRepository;
import com.example.pos.repository.PurchaseInvoiceRepository;
import com.example.pos.repository.SupplierRepository;
import com.example.pos.repository.SupplierTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstallmentService {

    private final InstallmentRepository installmentRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierTransactionRepository transactionRepository;
    private final AuditService auditService;

    @Transactional
    public List<InstallmentDto> generateInstallmentPlan(InstallmentPlanRequest request) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(request.getPurchaseInvoiceId())
                .orElseThrow(() -> new RuntimeException("Purchase Invoice not found"));

        if (invoice.getStatus() == PurchaseInvoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice is already fully paid.");
        }

        // Determine amount to be installed
        BigDecimal remainingAmount = invoice.getRemainingAmount();
        if (request.getDownPayment() != null && request.getDownPayment().compareTo(BigDecimal.ZERO) > 0) {
            if (request.getDownPayment().compareTo(remainingAmount) >= 0) {
                throw new RuntimeException("Down payment is greater than or equal to remaining amount.");
            }
            remainingAmount = remainingAmount.subtract(request.getDownPayment());
            
            // Record down payment as a Supplier payment if necessary, or just treat it as paid upfront.
            // For simplicity, we just reduce the invoice remaining amount here.
            invoice.setPaidAmount(invoice.getPaidAmount().add(request.getDownPayment()));
            invoice.setRemainingAmount(remainingAmount);
            
            Supplier supplier = invoice.getSupplier();
            supplier.setBalance(supplier.getBalance().subtract(request.getDownPayment()));
            supplierRepository.save(supplier);

            SupplierTransaction tx = SupplierTransaction.builder()
                    .supplier(supplier)
                    .amount(request.getDownPayment())
                    .type(SupplierTransaction.TransactionType.PAYMENT)
                    .transactionDate(LocalDateTime.now())
                    .description("Down payment for invoice " + invoice.getInvoiceNumber())
                    .balanceAfter(supplier.getBalance())
                    .build();
            transactionRepository.save(tx);
        }

        BigDecimal monthlyAmount = remainingAmount.divide(BigDecimal.valueOf(request.getNumberOfMonths()), 2, RoundingMode.HALF_UP);
        
        List<Installment> installments = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();
        BigDecimal currentAllocated = BigDecimal.ZERO;

        for (int i = 0; i < request.getNumberOfMonths(); i++) {
            BigDecimal amount = monthlyAmount;
            
            // Adjust last month to avoid rounding errors
            if (i == request.getNumberOfMonths() - 1) {
                amount = remainingAmount.subtract(currentAllocated);
            }

            Installment installment = Installment.builder()
                    .purchaseInvoice(invoice)
                    .amount(amount)
                    .dueDate(currentDate)
                    .status(Installment.InstallmentStatus.PENDING)
                    .build();

            installments.add(installment);
            currentAllocated = currentAllocated.add(amount);
            currentDate = currentDate.plusMonths(1);
        }

        purchaseInvoiceRepository.save(invoice);
        List<Installment> savedInstallments = installmentRepository.saveAll(installments);
        
        auditService.logAction("INSTALLMENT_GENERATE", "INSTALLMENT", invoice.getId(),
                "Generated " + request.getNumberOfMonths() + " installments for invoice " + invoice.getInvoiceNumber());

        return savedInstallments.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public InstallmentDto payInstallment(Long installmentId) {
        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        if (installment.getStatus() == Installment.InstallmentStatus.PAID) {
            throw new RuntimeException("Installment is already paid");
        }

        installment.setStatus(Installment.InstallmentStatus.PAID);
        installment.setPaymentDate(LocalDate.now());

        PurchaseInvoice invoice = installment.getPurchaseInvoice();
        
        // Update Invoice
        invoice.setPaidAmount(invoice.getPaidAmount().add(installment.getAmount()));
        invoice.setRemainingAmount(invoice.getRemainingAmount().subtract(installment.getAmount()));
        
        if (invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(PurchaseInvoice.InvoiceStatus.PAID);
            invoice.setRemainingAmount(BigDecimal.ZERO);
        } else {
            invoice.setStatus(PurchaseInvoice.InvoiceStatus.PARTIAL);
        }
        
        purchaseInvoiceRepository.save(invoice);

        // Update Supplier
        Supplier supplier = invoice.getSupplier();
        supplier.setBalance(supplier.getBalance().subtract(installment.getAmount()));
        supplierRepository.save(supplier);

        // Record Transaction
        SupplierTransaction tx = SupplierTransaction.builder()
                .supplier(supplier)
                .amount(installment.getAmount())
                .type(SupplierTransaction.TransactionType.PAYMENT)
                .transactionDate(LocalDateTime.now())
                .description("Paid installment for invoice " + invoice.getInvoiceNumber() + ", Due: " + installment.getDueDate())
                .balanceAfter(supplier.getBalance())
                .build();
        transactionRepository.save(tx);

        Installment savedInstallment = installmentRepository.save(installment);
        
        auditService.logAction("INSTALLMENT_PAY", "INSTALLMENT", savedInstallment.getId(),
                "Paid installment of " + savedInstallment.getAmount() + " for invoice " + invoice.getInvoiceNumber());

        return mapToDto(savedInstallment);
    }

    @Transactional(readOnly = true)
    public List<InstallmentDto> getInstallmentsForInvoice(Long invoiceId) {
        return installmentRepository.findByPurchaseInvoiceIdOrderByDueDateAsc(invoiceId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private InstallmentDto mapToDto(Installment installment) {
        return InstallmentDto.builder()
                .id(installment.getId())
                .invoiceId(installment.getPurchaseInvoice().getId())
                .invoiceNumber(installment.getPurchaseInvoice().getInvoiceNumber())
                .amount(installment.getAmount())
                .dueDate(installment.getDueDate())
                .paymentDate(installment.getPaymentDate())
                .status(installment.getStatus().name())
                .build();
    }
}
