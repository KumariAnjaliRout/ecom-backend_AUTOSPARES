package com.automobile.ecom.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductReviewResponse {

    private BigDecimal averageRating;
    private int totalReviews;
    private List<ReviewResponse> reviews;
}