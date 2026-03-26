package com.example.pos.controller;

import com.example.pos.dto.ApiResponse;
import com.example.pos.dto.SupplierDto;
import com.example.pos.dto.SupplierRequest;
import com.example.pos.entity.SupplierTransaction;
import com.example.pos.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier Management", description = "Endpoints for managing suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(summary = "Get all suppliers")
    public ResponseEntity<ApiResponse<List<SupplierDto>>> getAllSuppliers() {
        return ResponseEntity.ok(ApiResponse.<List<SupplierDto>>builder()
                .success(true)
                .data(supplierService.getAllSuppliers())
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(summary = "Get a single supplier")
    public ResponseEntity<ApiResponse<SupplierDto>> getSupplier(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<SupplierDto>builder()
                .success(true)
                .data(supplierService.getSupplierById(id, locale))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<ApiResponse<SupplierDto>> createSupplier(
            @Valid @RequestBody SupplierRequest request) {
        SupplierDto response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SupplierDto>builder()
                        .success(true)
                        .data(response)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<ApiResponse<SupplierDto>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request,
            Locale locale) {
        SupplierDto response = supplierService.updateSupplier(id, request, locale);
        return ResponseEntity.ok(ApiResponse.<SupplierDto>builder()
                .success(true)
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE')")
    @Operation(summary = "Delete a supplier")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id, Locale locale) {
        supplierService.deleteSupplier(id, locale);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }

    @GetMapping("/{id}/ledger")
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    @Operation(summary = "Get supplier transaction history")
    public ResponseEntity<ApiResponse<List<SupplierTransaction>>> getLedger(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<List<SupplierTransaction>>builder()
                .success(true)
                .data(supplierService.getSupplierLedger(id, locale))
                .build());
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    @Operation(summary = "Record a payment to a supplier")
    public ResponseEntity<ApiResponse<Void>> paySupplier(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            Locale locale) {
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String description = (String) payload.getOrDefault("description", "Manual Payment");
        supplierService.processPayment(id, amount, description, locale);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .build());
    }
}
