package com.automobile.ecom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)           // Fix: exclude null fields from JSON response
public class SubCategoryResponseDTO {

    // ─── Identity ─────────────────────────────────────────────
    private Long id;
    private String name;
    private String photoUrl;

    // ─── Status ───────────────────────────────────────────────
    private Boolean isActive;

    // ─── Parent Category ──────────────────────────────────────
    private Long categoryId;
    private String categoryName;

    // ─── Product Counts (useful for admin dashboard) ──────────
    private Integer productCount;
    private Integer activeProductCount;

    // ─── Timestamps ───────────────────────────────────────────
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")  // Fix: consistent date format for frontend
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}