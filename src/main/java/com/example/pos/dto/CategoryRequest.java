package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request object for creating or updating a category")
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Schema(description = "Name of the category", example = "Mobile Phones")
    private String name;
    
    @Schema(description = "Description of the category", example = "Smartphones and feature phones")
    private String description;
    
    @Schema(description = "ID of the parent category (optional)", example = "1")
    private Long parentId;
}
