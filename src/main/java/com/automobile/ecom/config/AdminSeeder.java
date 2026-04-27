package com.automobile.ecom.config;

import com.automobile.ecom.entity.Role;
import com.automobile.ecom.entity.User;
import com.automobile.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@gmail.com";

        if (userRepository.existsByEmail(adminEmail)) {
            System.out.println("✅ Admin already exists");
            return;
        }

        User admin = User.builder()
                .username("superadmin")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);

        System.out.println(" Default Admin Created");
    }
}