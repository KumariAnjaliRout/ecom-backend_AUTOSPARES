package com.automobile.ecom.document;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Document(indexName = "product", writeTypeHint = WriteTypeHint.FALSE)
//@Document(indexName = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {

    @Id
    private String id;

    // 🔹 Basic Info
    private String name;
    private String description;
    private String photoUrl;
    private String partNumber;
    private String company;

    // 🔹 Pricing
    private BigDecimal actualPrice;
    private Integer discount;
    private BigDecimal price;

    // 🔹 Inventory
    private Integer stockQuantity;
    private BigDecimal rating;
    private Boolean isActive;

    // 🔹 Category Hierarchy
    private String categoryId;
    private String categoryName;

    private String subCategoryId;
    private String subCategoryName;

    // 🔹 Vehicle Compatibility (SEARCH POWER 🔥)
    private List<String> compatibleVehicles;
    // 🔹 Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}