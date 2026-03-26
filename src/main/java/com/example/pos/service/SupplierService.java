package com.example.pos.service;

import com.example.pos.dto.SupplierDto;
import com.example.pos.dto.SupplierRequest;
import com.example.pos.entity.Supplier;
import com.example.pos.entity.SupplierTransaction;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.mapper.SupplierMapper;
import com.example.pos.repository.SupplierRepository;
import com.example.pos.repository.SupplierTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierTransactionRepository transactionRepository;
    private final SupplierMapper supplierMapper;
    private final MessageSource messageSource;
    private final AuditService auditService;

    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    public SupplierDto getSupplierById(Long id, Locale locale) {
        Supplier supplier = findSupplierOrThrow(id, locale);
        return supplierMapper.toDto(supplier);
    }

    @Transactional
    public SupplierDto createSupplier(SupplierRequest request) {
        Supplier supplier = supplierMapper.toEntity(request);
        supplierRepository.save(supplier);
        auditService.logAction("SUPPLIER_CREATE", "SUPPLIER", supplier.getId(), "Created supplier: " + supplier.getName());
        return supplierMapper.toDto(supplier);
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierRequest request, Locale locale) {
        Supplier supplier = findSupplierOrThrow(id, locale);
        supplierMapper.updateEntityFromRequest(request, supplier);
        supplierRepository.save(supplier);
        auditService.logAction("SUPPLIER_UPDATE", "SUPPLIER", supplier.getId(), "Updated supplier: " + supplier.getName());
        return supplierMapper.toDto(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id, Locale locale) {
        Supplier supplier = findSupplierOrThrow(id, locale);
        auditService.logAction("SUPPLIER_DELETE", "SUPPLIER", id, "Deleted supplier: " + supplier.getName());
        supplierRepository.delete(supplier);
    }

    public Supplier findSupplierOrThrow(Long id, Locale locale) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("error.not.found", null, locale)));
    }

    public List<SupplierTransaction> getSupplierLedger(Long supplierId, Locale locale) {
        findSupplierOrThrow(supplierId, locale);
        return transactionRepository.findBySupplierIdOrderByTransactionDateDesc(supplierId);
    }

    @Transactional
    public void processPayment(Long supplierId, BigDecimal amount, String description, Locale locale) {
        Supplier supplier = findSupplierOrThrow(supplierId, locale);

        // Update balance
        supplier.setBalance(supplier.getBalance().subtract(amount));
        supplierRepository.save(supplier);

        // Record transaction
        SupplierTransaction transaction = SupplierTransaction.builder()
                .supplier(supplier)
                .amount(amount)
                .type(SupplierTransaction.TransactionType.PAYMENT)
                .transactionDate(LocalDateTime.now())
                .description(description)
                .balanceAfter(supplier.getBalance())
                .build();
        transactionRepository.save(transaction);

        auditService.logAction("SUPPLIER_PAYMENT", "SUPPLIER", supplierId,
                "Paid " + amount + " to supplier " + supplier.getName());
    }
}
