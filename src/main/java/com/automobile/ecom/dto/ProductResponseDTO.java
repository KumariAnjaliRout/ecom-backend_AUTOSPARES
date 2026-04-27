package com.automobile.ecom.dto;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String photoUrl;
    private String partNumber;
    private String company;
    private BigDecimal actualPrice;
    private Integer discount;
    private BigDecimal price;
    private Integer stockQuantity;
    private BigDecimal rating;
    private Boolean isActive;

    private Long subCategoryId;
    private String subCategoryName;
    private String categoryName;

    // List for Admin Warning: "Fits: Toyota Camry 2022, etc."
    private List<String> compatibleVehicles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}