package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Category Data Transfer Object")
public class CategoryDto {
    @Schema(description = "Category ID", example = "1")
    private Long id;
    
    @Schema(description = "Name of the category", example = "Electronics")
    private String name;
    
    @Schema(description = "Description of the category", example = "Electronic devices and accessories")
    private String description;
    
    @Schema(description = "ID of the parent category, if any", example = "null")
    private Long parentId;
    
    @Schema(description = "Name of the parent category", example = "null")
    private String parentName;
    
    @Schema(description = "List of subcategories")
    private List<CategoryDto> children;
    
    @Schema(description = "Number of products in this category", example = "150")
    private Integer productCount;
}
