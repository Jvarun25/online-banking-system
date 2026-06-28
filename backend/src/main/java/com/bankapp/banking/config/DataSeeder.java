package com.bankapp.banking.config;

import com.bankapp.banking.entity.User;
import com.bankapp.banking.enums.Role;
import com.bankapp.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds a default admin user on every startup if one doesn't already exist,
 * purely so the RBAC admin endpoints are reachable out of the box without
 * manual SQL. In a real production system this would be replaced by a
 * proper migration (Flyway/Liquibase) and the password would never be a
 * hardcoded default.
 *
 * Default admin login (CHANGE IN PRODUCTION):
 *   username: admin
 *   password: Admin@123
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@bankapp.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("System Administrator")
                    .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Seeded default admin user (username: admin / password: Admin@123)");
        }
    }
}
