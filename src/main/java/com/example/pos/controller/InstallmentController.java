package com.example.pos.controller;

import com.example.pos.dto.InstallmentDto;
import com.example.pos.dto.InstallmentPlanRequest;
import com.example.pos.service.InstallmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/installments")
@RequiredArgsConstructor
@Tag(name = "Installment Management", description = "Endpoints for managing invoice installments and payments")
public class InstallmentController {

    private final InstallmentService installmentService;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    @Operation(summary = "Generate an installment plan", description = "Creates a series of future payments for a specific purchase invoice.")
    public ResponseEntity<List<InstallmentDto>> generateInstallmentPlan(@Valid @RequestBody InstallmentPlanRequest request) {
        return ResponseEntity.ok(installmentService.generateInstallmentPlan(request));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    @Operation(summary = "Pay an installment", description = "Marks an installment as paid and updates invoice and supplier balances.")
    public ResponseEntity<InstallmentDto> payInstallment(@PathVariable Long id) {
        return ResponseEntity.ok(installmentService.payInstallment(id));
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAuthority('PURCHASE_READ')")
    @Operation(summary = "Get installments for an invoice", description = "Retrieves all installments associated with a given purchase invoice ID.")
    public ResponseEntity<List<InstallmentDto>> getInstallmentsForInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(installmentService.getInstallmentsForInvoice(invoiceId));
    }
}
