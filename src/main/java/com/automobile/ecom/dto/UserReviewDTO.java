package com.automobile.ecom.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReviewDTO {

    private Long reviewId;

    private Long productId;
    private String productName;

    private Long orderId;

    private int rating;
    private String comment;

    private LocalDateTime createdAt;
}
