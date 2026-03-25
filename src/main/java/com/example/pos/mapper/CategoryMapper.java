package com.example.pos.mapper;

import com.example.pos.dto.CategoryDto;
import com.example.pos.dto.CategoryRequest;
import com.example.pos.entity.Category;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) return null;
        
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .children(category.getChildren() != null ? 
                        category.getChildren().stream().map(this::toDto).collect(Collectors.toList()) : null)
                .build();
    }

    public Category toEntity(CategoryRequest request) {
        if (request == null) return null;
        
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }
}
