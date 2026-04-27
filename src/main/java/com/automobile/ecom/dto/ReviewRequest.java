package com.automobile.ecom.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    private Long productId;
    private Long orderId;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;
}