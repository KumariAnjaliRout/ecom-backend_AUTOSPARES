package com.automobile.ecom.controller;



import com.automobile.ecom.dto.ProductReviewResponse;
import com.automobile.ecom.dto.ReviewRequest;
import com.automobile.ecom.dto.UserReviewDTO;
import com.automobile.ecom.entity.Product;
import com.automobile.ecom.security.CustomUserPrincipal;
import com.automobile.ecom.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    // ================= ADD REVIEW =================
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public String addReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        UUID userId = user.getUserId();
        log.info("User {} adding review for product {}", userId, request.getProductId());
        reviewService.addReview(request, userId);
        return "Review added successfully";
    }

    // ================= GET REVIEWS FOR PRODUCT =================
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ProductReviewResponse getReviews(@PathVariable Long productId) {
        log.info("Fetching reviews for product {}", productId);
        return reviewService.getReviews(productId);
    }


    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<UserReviewDTO>> getUserReviews(
            @AuthenticationPrincipal CustomUserPrincipal user) {
        UUID userId = user.getUserId();
        log.info("Fetching user reviews for {}", userId);
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }

//    @PatchMapping("/{reviewId}")
//    public ResponseEntity<String> updateReview(
//            @PathVariable Long reviewId,
//            @RequestBody ReviewRequest request,
//            @AuthenticationPrincipal CustomUserPrincipal user) {
//        UUID userId = user.getUserId();
//        reviewService.updateReview(reviewId, request, userId);
//        return ResponseEntity.ok("Review updated successfully");
//    }

}
