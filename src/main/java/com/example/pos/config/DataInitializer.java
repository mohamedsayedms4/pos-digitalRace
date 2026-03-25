package com.example.pos.config;

import com.example.pos.entity.Permission;
import com.example.pos.entity.Role;
import com.example.pos.entity.User;
import com.example.pos.repository.PermissionRepository;
import com.example.pos.repository.RoleRepository;
import com.example.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking database seed...");

        // 1. Ensure all permissions exist
        List<String> permNames = Arrays.asList(
                "USER_READ", "USER_WRITE", "USER_DELETE",
                "ROLE_READ", "ROLE_WRITE", "ROLE_DELETE",
                "PRODUCT_READ", "PRODUCT_WRITE", "PRODUCT_DELETE",
                "CATEGORY_READ", "CATEGORY_WRITE", "CATEGORY_DELETE",
                "AUDIT_READ"
        );

        Set<Permission> allPermissions = new HashSet<>();
        for (String name : permNames) {
            Permission p = permissionRepository.findByName(name)
                    .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
            allPermissions.add(p);
        }

        // 2. Sync ADMIN role with all permissions if it exists
        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole -> {
            adminRole.setPermissions(allPermissions);
            roleRepository.save(adminRole);
        });

        if (roleRepository.count() > 0) {
            log.info("Database roles already seeded. Missing permissions were synced.");
            return;
        }

        log.info("Seeding initial roles and permissions...");

        // 3. Create Roles (only if completely empty)
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .permissions(allPermissions)
                .build();
        roleRepository.save(adminRole);

        Role userRole = Role.builder()
                .name("ROLE_USER")
                .build();
        roleRepository.save(userRole);

        // 3. Create Admin User
        User admin = User.builder()
                .name("System Admin")
                .email("admin@pos.com")
                .password(passwordEncoder.encode("Admin@123"))
                .roles(Set.of(adminRole))
                .enabled(true)
                .build();
        userRepository.save(admin);

        log.info("Seeding completed successfully: Admin user 'admin@pos.com' / 'Admin@123'");
    }
}
