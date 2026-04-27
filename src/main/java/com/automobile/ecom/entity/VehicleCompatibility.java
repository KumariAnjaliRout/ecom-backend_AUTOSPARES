package com.automobile.ecom.entity;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vehicle_compatibility_search",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"product_id", "vehicle_id"},
                        name = "uk_product_vehicle_compatibility"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_compatibility_product"))
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false, foreignKey = @ForeignKey(name = "fk_compatibility_vehicle"))
    private Vehicle vehicle;

    @OneToOne(mappedBy = "vehicleCompatibility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CompatibilityDetails compatibilityDetails;

    // ─── DELEGATION METHODS TO FIX ERRORS ──────────────────────

    public String getBrand() {
        return (vehicle != null) ? vehicle.getBrand() : null;
    }

    public String getModel() {
        return (vehicle != null) ? vehicle.getModel() : null;
    }

    public Integer getYear() {
        return (vehicle != null) ? vehicle.getYear() : null;
    }
}
