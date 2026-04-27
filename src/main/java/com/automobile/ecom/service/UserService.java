package com.automobile.ecom.service;


import com.automobile.ecom.dto.PageResponse;
import com.automobile.ecom.dto.UserResponse;
import com.automobile.ecom.entity.Role;
import com.automobile.ecom.entity.User;
import com.automobile.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // COMMON BUILDER
    private PageResponse<UserResponse> buildResponse(Page<User> page) {

        List<UserResponse> data = page.getContent()
                .stream()
                .map(UserResponse::from)
                .toList();

        return PageResponse.<UserResponse>builder()
                .data(data)
                .currentPage(page.getNumber())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    //  ALL USERS
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        return buildResponse( userRepository.findAllByRole(Role.USER, pageable));
    }

    // ONLY ADMINS
    public PageResponse<UserResponse> getAllAdmins(Pageable pageable) {
        return buildResponse(
                userRepository.findAllByRole(Role.ADMIN, pageable)
        );
    }

}