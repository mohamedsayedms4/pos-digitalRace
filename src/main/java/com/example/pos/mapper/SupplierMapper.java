package com.example.pos.mapper;

import com.example.pos.dto.SupplierDto;
import com.example.pos.dto.SupplierRequest;
import com.example.pos.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierDto toDto(Supplier supplier);

    Supplier toEntity(SupplierRequest request);

    void updateEntityFromRequest(SupplierRequest request, @MappingTarget Supplier supplier);
}
