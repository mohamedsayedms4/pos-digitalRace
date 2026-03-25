package com.example.pos.service;

import com.example.pos.dto.AssignRolesRequest;
import com.example.pos.dto.UpdateAccessRequest;
import com.example.pos.dto.CreateUserRequest;
import com.example.pos.dto.UserDto;
import com.example.pos.entity.Permission;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.exception.ResourceNotFoundException;
import com.example.pos.mapper.UserMapper;
import com.example.pos.repository.PermissionRepository;
import com.example.pos.repository.RoleRepository;
import com.example.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final AuditService auditService;

    /* -------- LIST -------- */

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /* -------- GET ONE -------- */

    public UserDto getUserById(Long id, Locale locale) {
        User user = findUserOrThrow(id, locale);
        return userMapper.toDto(user);
    }

    /* -------- CREATE -------- */

    @Transactional
    public UserDto createUser(CreateUserRequest request, Locale locale) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(messageSource.getMessage("auth.email.exists", null, locale));
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(request.isEnabled())
                .build();

        if (request.getRoles() != null) {
            user.setRoles(mapRoles(request.getRoles(), locale));
        }
        if (request.getPermissions() != null) {
            user.setPermissions(mapPermissions(request.getPermissions(), locale));
        }

        User saved = userRepository.save(user);
        auditService.logAction("USER_CREATE", "USER", saved.getId(), "Created user: " + saved.getEmail());
        return userMapper.toDto(saved);
    }

    /* -------- UPDATE ACCESS -------- */

    @Transactional
    public UserDto updateAccess(Long userId, UpdateAccessRequest request, Locale locale) {
        User user = findUserOrThrow(userId, locale);

        if (request.getRoles() != null) {
            user.setRoles(mapRoles(request.getRoles(), locale));
        }
        if (request.getPermissions() != null) {
            user.setPermissions(mapPermissions(request.getPermissions(), locale));
        }

        userRepository.save(user);
        return userMapper.toDto(user);
    }

    /* -------- ASSIGN ROLES -------- */

    @Transactional
    public UserDto assignRoles(Long userId, AssignRolesRequest request, Locale locale) {
        User user = findUserOrThrow(userId, locale);
        user.setRoles(mapRoles(request.getRoles(), locale));
        userRepository.save(user);
        auditService.logAction("USER_ROLE_ASSIGN", "USER", user.getId(), "Assigned roles to user: " + user.getEmail());
        return userMapper.toDto(user);
    }

    /* -------- METADATA -------- */

    public List<String> getAllRoles() {
        return roleRepository.findAll().stream().map(Role::getName).collect(Collectors.toList());
    }

    public List<String> getAllPermissions() {
        return permissionRepository.findAll().stream().map(Permission::getName).collect(Collectors.toList());
    }

    /* -------- ENABLE / DISABLE -------- */

    @Transactional
    public UserDto setEnabled(Long userId, boolean enabled, Locale locale) {
        User user = findUserOrThrow(userId, locale);
        user.setEnabled(enabled);
        userRepository.save(user);
        auditService.logAction(enabled ? "USER_ENABLE" : "USER_DISABLE", "USER", user.getId(), (enabled ? "Enabled" : "Disabled") + " user: " + user.getEmail());
        return userMapper.toDto(user);
    }

    /* -------- DELETE -------- */

    @Transactional
    public void deleteUser(Long userId, Locale locale) {
        User user = findUserOrThrow(userId, locale);
        auditService.logAction("USER_DELETE", "USER", user.getId(), "Deleted user: " + user.getEmail());
        userRepository.delete(user);
    }

    /* -------- HELPER -------- */

    private User findUserOrThrow(Long id, Locale locale) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageFormat.format(
                                messageSource.getMessage("admin.user.not.found", null, locale), id)
                ));
    }

    private Set<Role> mapRoles(Set<String> roleNames, Locale locale) {
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            String normalized = name.startsWith("ROLE_") ? name : "ROLE_" + name;
            roles.add(roleRepository.findByName(normalized)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageSource.getMessage("admin.role.not.found", new Object[]{name}, locale))));
        }
        return roles;
    }

    private Set<Permission> mapPermissions(Set<String> permNames, Locale locale) {
        Set<Permission> perms = new HashSet<>();
        for (String name : permNames) {
            perms.add(permissionRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            messageSource.getMessage("admin.permission.not.found", new Object[]{name}, locale))));
        }
        return perms;
    }
}
