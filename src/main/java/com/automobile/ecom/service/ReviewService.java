package com.automobile.ecom.service;


import com.automobile.ecom.dto.ProductReviewResponse;
import com.automobile.ecom.dto.ReviewRequest;
import com.automobile.ecom.dto.UserReviewDTO;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    void addReview(ReviewRequest request, UUID userId);

    ProductReviewResponse getReviews(Long productId);
    List<UserReviewDTO> getUserReviews(UUID userId);
}