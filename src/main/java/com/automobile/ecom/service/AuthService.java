package com.automobile.ecom.service;

import com.automobile.ecom.dto.*;
import com.automobile.ecom.entity.*;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.exception.UnauthorizedException;
import com.automobile.ecom.repository.UserRepository;
import com.automobile.ecom.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .build();

        userRepository.save(user);
        auditService.logAction(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                "USER_REGISTERED",
                "USER",
                user.getId().toString(),
                "New user registered"
        );
        return "User registered successfully. Please login.";
    }


    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            //  LOGIN FAILED
            auditService.logAction(
                    null,
                    request.getEmail(),   // we don't have username → use email
                    "UNKNOWN",
                    "LOGIN_FAILED",
                    "USER",
                    null,
                    "Failed login attempt"
            );

            throw new UnauthorizedException("Invalid email and password");
        }

        String token = jwtUtil.generateToken(user);

        // LOGIN SUCCESS
        auditService.logAction(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                "LOGIN_SUCCESS",
                "USER",
                user.getId().toString(),
                "User logged in successfully"
        );

        return new AuthResponse(token);
    }

    public void createAdmin(CreateAdminRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);
    }

    public void changePassword(UUID userId, ChangePassword request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            auditService.logAction(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name(),
                    "PASSWORD_CHANGE_FAILED",
                    "USER",
                    user.getId().toString(),
                    "Incorrect old password"
            );
            throw new BadRequestException("Old password is incorrect");
        }

        // 2. Prevent reuse
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be same as old password");
        }

        // 3. Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        //  PASSWORD CHANGE AUDIT
        auditService.logAction(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                "PASSWORD_CHANGED",
                "USER",
                user.getId().toString(),
                "User changed password"
        );
    }

    public void logout(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        auditService.logAction(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                "LOGOUT",
                "USER",
                user.getId().toString(),
                "User logged out"
        );
    }

}