package com.recruitpro.config;

import com.recruitpro.entity.Role;
import com.recruitpro.entity.User;
import com.recruitpro.enums.RoleName;
import com.recruitpro.repository.RoleRepository;
import com.recruitpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Seeds the database with the four roles and a default Admin user
 * on first startup if they don't already exist.
 *
 * Change the admin credentials via environment variables before deploying:
 *   ADMIN_EMAIL / ADMIN_PASSWORD
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository  roleRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedAdminUser() {
        String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@recruitpro.com");
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin@12345");
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found after seeding"));

        User admin = User.builder()
                .firstName("System")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .roles(Set.of(adminRole))
                .enabled(true)
                .accountNonLocked(true)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created: {}", adminEmail);
        log.warn("Change the default admin password before going to production!");
    }
}
