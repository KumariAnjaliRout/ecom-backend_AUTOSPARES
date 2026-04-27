package com.automobile.ecom.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String photoUrl;
    private Boolean isActive;
    private Integer subCategoryCount;
    private Integer activeSubCategoryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}