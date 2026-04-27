package com.automobile.ecom.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {

    private Long id;
    private Long productId;
    private String productName;

    // must map from product.getPhotoUrl()
    private String productImage;

    // Product.price (BigDecimal → Double)
    private Double price;

    private String description;
    private String company;
    private Integer stockQuantity;
    private Boolean isActive;
    private String categoryName;
    private String subCategoryName;
    private Double actualPrice;
    private Double discount;

    private LocalDateTime addedAt;
}

