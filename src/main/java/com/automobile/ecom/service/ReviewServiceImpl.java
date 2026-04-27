package com.automobile.ecom.service;

import com.automobile.ecom.document.ProductDocument;
import com.automobile.ecom.dto.ProductReviewResponse;
import com.automobile.ecom.dto.ReviewRequest;
import com.automobile.ecom.dto.ReviewResponse;
import com.automobile.ecom.dto.UserReviewDTO;
import com.automobile.ecom.entity.*;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;
    private final ProductSearchRepository productsearchRepository;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    // ================= ADD REVIEW =================
    @Override
    public void addReview(ReviewRequest request, UUID userId) {

        // ================= USER =================
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ================= PRODUCT =================
        Product product = productRepo.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ================= ORDER =================
        Order order = orderRepo.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // ================= VALIDATIONS =================

        // 1⃣ Order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Invalid order access");
        }

        //  ONLY DELIVERED ORDER CAN REVIEW (IMPORTANT)
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("You can review only after order is delivered");
        }

        // 3⃣ Product exists in that order
        boolean purchased = order.getOrderItems().stream()
                .anyMatch(item ->
                        item.getProduct().getId().equals(product.getId())
                );

        if (!purchased) {
            throw new BadRequestException("You can only review purchased products");
        }

        // 4️ Prevent duplicate review
        if (reviewRepo.existsByUserAndProductAndOrder(user, product, order)) {
            throw new BadRequestException("Already reviewed this product");
        }

        // ================= CREATE REVIEW =================
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setOrder(order);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        reviewRepo.save(review);

        // ================= UPDATE PRODUCT RATING =================
        updateProductRating(product, request.getRating());
    }

    // ================= GET REVIEWS =================
    @Override
    public ProductReviewResponse getReviews(Long productId) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Review> reviews = reviewRepo.findByProduct(product);

        List<ReviewResponse> responseList = reviews.stream().map(r -> {
            ReviewResponse res = new ReviewResponse();
            res.setUsername(r.getUser().getUsername());
            res.setRating(r.getRating());
            res.setComment(r.getComment());
            res.setCreatedAt(r.getCreatedAt());
            return res;
        }).toList();

        ProductReviewResponse response = new ProductReviewResponse();
        response.setAverageRating(product.getRating());
        response.setTotalReviews(product.getTotalReviews());
        response.setReviews(responseList);

        return response;
    }

    // ================= RATING LOGIC =================
    private void updateProductRating(Product product, int newRating) {

        double currentAvg = product.getRating().doubleValue();
        int total = product.getTotalReviews();

        double newAvg = ((currentAvg * total) + newRating) / (total + 1);

        product.setRating(BigDecimal.valueOf(newAvg));
        product.setTotalReviews(total + 1);

        Product saved = productRepo.save(product);

        // 🔥 INDEX INTO ELASTICSEARCH
        try {
            ProductDocument doc = buildDocument(saved);
            productsearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
    }
    @Override
    public List<UserReviewDTO> getUserReviews(UUID userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Review> reviews = reviewRepo.findByUser(user);

        return reviews.stream().map(r -> {

            UserReviewDTO dto = new UserReviewDTO();

            dto.setReviewId(r.getId());
            dto.setRating(r.getRating());
            dto.setComment(r.getComment());
            dto.setCreatedAt(r.getCreatedAt());

            // Product mapping
            if (r.getProduct() != null) {
                dto.setProductId(r.getProduct().getId());
            }

            //  Order mapping
            if (r.getOrder() != null) {
                dto.setOrderId(r.getOrder().getId());
            }

            return dto;

        }).toList();
    }
    public ProductDocument buildDocument(Product product) {

        // 🔥 Build ProductDocument (exactly your structure)
        return ProductDocument.builder()
                .id(product.getId().toString())

                // 🔹 Basic Info
                .name(product.getName())
                .description(product.getDescription())
                .photoUrl(product.getPhotoUrl())
                .partNumber(product.getPartNumber())
                .company(product.getCompany())

                // 🔹 Pricing
                .actualPrice(product.getActualPrice())
                .discount(product.getDiscount())
                .price(product.getPrice())

                // 🔹 Inventory
                .stockQuantity(product.getStockQuantity())
                .rating(product.getRating())
                .isActive(product.getIsActive())

                // 🔹 Category Hierarchy
                .categoryId(product.getSubCategory().getCategory().getId().toString())
                .categoryName(product.getSubCategory().getCategory().getName())

                .subCategoryId(product.getSubCategory().getId().toString())
                .subCategoryName(product.getSubCategory().getName())

                // 🔹 Metadata
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())

                .build();
    }
}