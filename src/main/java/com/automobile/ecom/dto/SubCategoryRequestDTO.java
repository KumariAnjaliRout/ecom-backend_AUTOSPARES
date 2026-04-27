package com.automobile.ecom.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryRequestDTO {
    @NotBlank(message = "SubCategory name is required")
    @Size(min = 2, max = 100, message = "SubCategory name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Photo URL is required")
    private String photoUrl;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}