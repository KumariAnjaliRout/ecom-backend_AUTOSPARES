package com.automobile.ecom.controller;

import com.automobile.ecom.dto.*;
import com.automobile.ecom.entity.User;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.exception.UnauthorizedException;
import com.automobile.ecom.repository.UserRepository;
import com.automobile.ecom.security.CustomUserPrincipal;
import com.automobile.ecom.security.JwtUtil;
import com.automobile.ecom.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        authService.register(request);

        return ResponseEntity.ok(
                Map.of("message", "User registered successfully. Please login.")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

//    @PostMapping("/refresh")
//    public AuthResponse refreshToken(@RequestBody RefreshRequest request) {
//
//        String refreshToken = request.getRefreshToken();
//
//        if (!jwtUtil.validateToken(refreshToken)) {
//            throw new UnauthorizedException("Invalid refresh token");
//        }
//
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(jwtUtil.getKey())
//                .build()
//                .parseClaimsJws(refreshToken)
//                .getBody();
//
//        if (!"refresh".equals(claims.get("type"))) {
//            throw new UnauthorizedException("Invalid token type");
//        }
//
//        UUID userId = UUID.fromString(claims.getSubject());
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        if (!user.isActive()) {
//            throw new UnauthorizedException("User account disabled");
//        }
//
//        String newAccessToken = jwtUtil.generateToken(user);
//
//        return new AuthResponse(newAccessToken, refreshToken);
//    }

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createAdmin(
            @RequestBody CreateAdminRequest request
    ) {
        authService.createAdmin(request);
        return ResponseEntity.ok("Admin created successfully");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody @Valid ChangePassword request
    ) {
        authService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(
                Map.of("message", "Password updated successfully")
        );
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            HttpServletRequest request
    ) {
        authService.logout(
                principal.getUserId()
        );

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }
}