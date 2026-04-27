package com.automobile.ecom.controller;


import com.automobile.ecom.dto.WishlistResponse;
import com.automobile.ecom.security.CustomUserPrincipal;
import com.automobile.ecom.service.WishlistService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    // ================= GET =================
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WishlistResponse> getWishlist(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        return ResponseEntity.ok(
                wishlistService.getWishlist(principal.getUserId())
        );
    }

    // ================= ADD =================
    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        wishlistService.addToWishlist(
                principal.getUserId(),
                productId
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ================= REMOVE =================
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        wishlistService.removeFromWishlist(
                principal.getUserId(),
                productId
        );

        return ResponseEntity.ok().build();
    }

    // ================= CLEAR =================
    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearWishlist(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        wishlistService.clearWishlist(principal.getUserId());

        return ResponseEntity.ok().build();
    }
}
