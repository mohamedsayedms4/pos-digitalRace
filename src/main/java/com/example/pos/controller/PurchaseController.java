package com.example.pos.controller;

import com.example.pos.dto.ApiResponse;
import com.example.pos.dto.PurchaseInvoiceDto;
import com.example.pos.dto.PurchaseInvoiceRequest;
import com.example.pos.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchase Management", description = "Endpoints for recording purchase invoices and managing stock procurement")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    @Operation(summary = "Record a new purchase invoice", description = "Creates an invoice, updates product stock, and updates supplier balance.")
    public ResponseEntity<ApiResponse<PurchaseInvoiceDto>> createPurchaseInvoice(
            @Valid @RequestBody PurchaseInvoiceRequest request,
            Locale locale) {
        PurchaseInvoiceDto response = purchaseService.createPurchaseInvoice(request, locale);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PurchaseInvoiceDto>builder()
                        .success(true)
                        .data(response)
                        .build());
    }
}
