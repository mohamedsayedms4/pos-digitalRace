package com.example.pos.controller;

import com.example.pos.dto.ApiResponse;
import com.example.pos.dto.CategoryDto;
import com.example.pos.dto.CategoryRequest;
import com.example.pos.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageSource messageSource;

    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories(
            @RequestParam(defaultValue = "false") boolean rootsOnly) {
        return ResponseEntity.ok(ApiResponse.<List<CategoryDto>>builder()
                .success(true)
                .data(categoryService.getAllCategories(rootsOnly))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategory(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<CategoryDto>builder()
                .success(true)
                .data(categoryService.getCategoryById(id, locale))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CategoryRequest request, Locale locale) {
        CategoryDto response = categoryService.createCategory(request, locale);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CategoryDto>builder()
                        .success(true)
                        .message(messageSource.getMessage("admin.category.created", null, locale))
                        .data(response)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_WRITE')")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            Locale locale) {
        CategoryDto response = categoryService.updateCategory(id, request, locale);
        return ResponseEntity.ok(ApiResponse.<CategoryDto>builder()
                .success(true)
                .message(messageSource.getMessage("admin.category.updated", null, locale))
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id, Locale locale) {
        categoryService.deleteCategory(id, locale);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(messageSource.getMessage("admin.category.deleted", null, locale))
                .build());
    }
}
