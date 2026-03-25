package com.example.pos.mapper;

import com.example.pos.dto.RegisterRequest;
import com.example.pos.dto.UserDto;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "permissionsToNames")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "password", ignore = true)  // hashed separately in service
    User toEntity(RegisterRequest request);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    @Named("permissionsToNames")
    default Set<String> permissionsToNames(Set<com.example.pos.entity.Permission> permissions) {
        if (permissions == null) return Set.of();
        return permissions.stream().map(com.example.pos.entity.Permission::getName).collect(Collectors.toSet());
    }
}
