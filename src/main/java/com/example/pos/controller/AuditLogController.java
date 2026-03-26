package com.example.pos.controller;

import com.example.pos.dto.ApiResponse;
import com.example.pos.dto.AuditLogDto;
import com.example.pos.entity.AuditLog;
import com.example.pos.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints for viewing system audit history (Requires AUDIT_READ)")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_READ')")
    @Operation(summary = "Get paginated audit logs", description = "Retrieves system audit logs ordered by newest first.")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AuditLog> logPage = auditLogRepository.findAll(
                PageRequest.of(page, size, Sort.by("timestamp").descending()));
        
        List<AuditLogDto> dtos = logPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<AuditLogDto>>builder()
                .success(true)
                .data(dtos)
                .build());
    }

    private AuditLogDto toDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .username(log.getUsername())
                .action(log.getAction())
                .resource(log.getResource())
                .resourceId(log.getResourceId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .build();
    }
}
