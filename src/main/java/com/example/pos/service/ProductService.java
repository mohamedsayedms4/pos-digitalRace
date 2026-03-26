package com.example.pos.service;

import com.example.pos.dto.ProductDto;
import com.example.pos.dto.ProductRequest;
import com.example.pos.entity.Product;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.entity.Category;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.mapper.ProductMapper;
import com.example.pos.repository.CategoryRepository;
import com.example.pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final MessageSource messageSource;
    private final AuditService auditService;
    private final FileStorageService fileStorageService;
    private final BarcodeService barcodeService;

    public String getProductBarcode(Long id, Locale locale) {
        Product product = findProductOrThrow(id, locale);
        // التغيير: الباركود سيحتوي على الـ id فقط
        return barcodeService.generateBarcodeBase64(product.getId().toString(), 300, 100);
    }

    public String getProductQrCode(Long id, Locale locale) {
        Product product = findProductOrThrow(id, locale);
        // التغيير: الـ QR سيحتوي على الاسم والوصف
        String qrContent = String.format("Name: %s\nDescription: %s", 
                product.getName(), 
                product.getDescription() != null ? product.getDescription() : "");
        return barcodeService.generateQRCodeBase64(qrContent, 300, 300);
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id, Locale locale) {
        Product product = findProductOrThrow(id, locale);
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductRequest request, org.springframework.web.multipart.MultipartFile[] imageFiles) {
        Product product = productMapper.toEntity(request);
        
        if (product.getProductCode() == null || product.getProductCode().isBlank()) {
            product.setProductCode("PRD-" + System.currentTimeMillis());
        }

        if (product.getStock() == null) {
            product.setStock(0);
        }
        
        if (request.getCategoryId() != null) {
            product.setCategory(findCategoryOrThrow(request.getCategoryId(), null));
        }

        if (imageFiles != null && imageFiles.length > 0) {
            for (org.springframework.web.multipart.MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    com.example.pos.entity.ProductImage image = com.example.pos.entity.ProductImage.builder()
                            .imageUrl("/api/v1/products/images/" + fileName)
                            .fileName(fileName)
                            .contentType(file.getContentType())
                            .product(product)
                            .build();
                    product.getImages().add(image);
                }
            }
        }
        
        productRepository.save(product);
        auditService.logAction("PRODUCT_CREATE", "PRODUCT", product.getId(), "Created product: " + product.getName());
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductRequest request, org.springframework.web.multipart.MultipartFile[] imageFiles, Locale locale) {
        Product product = findProductOrThrow(id, locale);
        productMapper.updateEntityFromRequest(request, product);
        
        if (product.getStock() == null) {
            product.setStock(0);
        }

        if (request.getCategoryId() != null) {
            product.setCategory(findCategoryOrThrow(request.getCategoryId(), locale));
        } else {
            product.setCategory(null);
        }

        if (imageFiles != null && imageFiles.length > 0) {
            for (org.springframework.web.multipart.MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    com.example.pos.entity.ProductImage image = com.example.pos.entity.ProductImage.builder()
                            .imageUrl("/api/v1/products/images/" + fileName)
                            .fileName(fileName)
                            .contentType(file.getContentType())
                            .product(product)
                            .build();
                    product.getImages().add(image);
                }
            }
        }
        
        productRepository.save(product);
        auditService.logAction("PRODUCT_UPDATE", "PRODUCT", product.getId(), "Updated product: " + product.getName());
        return productMapper.toDto(product);
    }

    @Transactional
    public void deleteProduct(Long id, Locale locale) {
        Product product = findProductOrThrow(id, locale);
        auditService.logAction("PRODUCT_DELETE", "PRODUCT", id, "Deleted product: " + product.getName());
        productRepository.delete(product);
    }

    private Product findProductOrThrow(Long id, Locale locale) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("error.not.found", null, locale)));
    }

    private Category findCategoryOrThrow(Long id, Locale locale) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("admin.category.not.found", new Object[]{id}, locale)));
    }
}
