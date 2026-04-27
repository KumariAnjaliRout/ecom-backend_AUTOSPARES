package com.automobile.ecom.dto;


import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    private String imageUrl;

    @NotBlank(message = "Part number is required")
    @Size(min = 2, max = 100, message = "Part number must be between 2 and 100 characters")
    private String partNumber;

    @NotBlank(message = "Company is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String company;

    @NotNull(message = "Actual price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Actual price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    private BigDecimal actualPrice;

    @NotNull(message = "Discount is required")
    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    private Integer discount;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "SubCategory ID is required")
    private Long subCategoryId;
}