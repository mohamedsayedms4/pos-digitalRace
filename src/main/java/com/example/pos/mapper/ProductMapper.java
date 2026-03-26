package com.example.pos.mapper;

import com.example.pos.dto.ProductDto;
import com.example.pos.dto.ProductRequest;
import com.example.pos.entity.Product;
import com.example.pos.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "imageUrls", expression = "java(product.getImages().stream().map(com.example.pos.entity.ProductImage::getImageUrl).toList())")
    ProductDto toDto(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
