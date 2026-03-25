package com.example.pos.service;

import com.example.pos.dto.CategoryDto;
import com.example.pos.dto.CategoryRequest;
import com.example.pos.entity.Category;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.mapper.CategoryMapper;
import com.example.pos.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final MessageSource messageSource;
    private final AuditService auditService;

    public List<CategoryDto> getAllCategories(boolean rootsOnly) {
        List<Category> categories = rootsOnly ? 
                categoryRepository.findByParentIsNull() : 
                categoryRepository.findAll();
        
        return categories.stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long id, Locale locale) {
        Category category = findCategoryOrThrow(id, locale);
        return categoryMapper.toDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryRequest request, Locale locale) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException(messageSource.getMessage("admin.category.exists", null, locale));
        }

        Category category = categoryMapper.toEntity(request);
        if (request.getParentId() != null) {
            category.setParent(findCategoryOrThrow(request.getParentId(), locale));
        }

        Category saved = categoryRepository.save(category);
        auditService.logAction("CATEGORY_CREATE", "CATEGORY", saved.getId(), "Created category: " + saved.getName());
        return categoryMapper.toDto(saved);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryRequest request, Locale locale) {
        Category category = findCategoryOrThrow(id, locale);
        
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException(messageSource.getMessage("admin.category.exists", null, locale));
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("Category cannot be its own parent");
            }
            category.setParent(findCategoryOrThrow(request.getParentId(), locale));
        } else {
            category.setParent(null);
        }

        Category saved = categoryRepository.save(category);
        auditService.logAction("CATEGORY_UPDATE", "CATEGORY", saved.getId(), "Updated category: " + saved.getName());
        return categoryMapper.toDto(saved);
    }

    @Transactional
    public void deleteCategory(Long id, Locale locale) {
        Category category = findCategoryOrThrow(id, locale);
        
        if (!category.getChildren().isEmpty()) {
            throw new RuntimeException(messageSource.getMessage("admin.category.has.children", null, locale));
        }
        
        if (!category.getProducts().isEmpty()) {
            throw new RuntimeException(messageSource.getMessage("admin.category.has.products", null, locale));
        }

        auditService.logAction("CATEGORY_DELETE", "CATEGORY", id, "Deleted category: " + category.getName());
        categoryRepository.delete(category);
    }

    public Category findCategoryOrThrow(Long id, Locale locale) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("admin.category.not.found", new Object[]{id}, locale)));
    }
}
