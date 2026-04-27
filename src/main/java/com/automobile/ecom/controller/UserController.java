package com.automobile.ecom.controller;


import com.automobile.ecom.dto.PageResponse;
import com.automobile.ecom.dto.UserResponse;
import com.automobile.ecom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //GET ALL USERS
    @GetMapping("/users/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            Pageable pageable) {

        return ResponseEntity.ok(
                userService.getAllUsers(pageable)
        );
    }

    // GET ALL ADMINS
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllAdmins(
            Pageable pageable) {

        return ResponseEntity.ok(
                userService.getAllAdmins(pageable)
        );
    }

}
