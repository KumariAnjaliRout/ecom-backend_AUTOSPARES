package com.automobile.ecom.dto;


import com.automobile.ecom.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserResponse {

    private UUID userId;
    private String username;
    private String email;
    private String role;
    private boolean isActive;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .build();
    }
}