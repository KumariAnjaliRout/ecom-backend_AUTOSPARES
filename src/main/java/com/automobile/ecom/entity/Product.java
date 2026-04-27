package com.automobile.ecom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_search", uniqueConstraints = {
        @UniqueConstraint(columnNames = "part_number", name = "uk_product_part_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "part_number", nullable = false, length = 100)
    private String partNumber;

    @Column(nullable = false, length = 100)
    private String company;

    @Column(name = "actual_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualPrice;

    @Column(nullable = false)
    private Integer discount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Calculated automatically

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    // 🏎️ CASCADE: Deleting a product deletes its compatibility rows automatically
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleCompatibility> vehicleCompatibilities = new ArrayList<>();

    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void calculatePrice() {
        if (actualPrice != null && discount != null) {
            BigDecimal discountFactor = BigDecimal.valueOf(100 - discount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            this.price = actualPrice.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
        }
    }

}