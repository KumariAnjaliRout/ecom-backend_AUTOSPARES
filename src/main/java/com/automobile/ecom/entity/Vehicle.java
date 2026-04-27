package com.automobile.ecom.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "vehicle_search",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"model", "year", "engine", "fuel_type"},
                        name = "uk_vehicle_model_year_engine_fuel"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Brand is required")
    @Size(min = 2, max = 100, message = "Brand must be between 2 and 100 characters")
    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 100, message = "Model must be between 1 and 100 characters")
    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1980, message = "Year must be after 1980")
    @Max(value = 2100, message = "Invalid year")
    @Column(name = "year", nullable = false)
    private Integer year;

    @NotBlank(message = "Engine is required")
    @Size(min = 1, max = 100, message = "Engine must be between 1 and 100 characters")
    @Column(name = "engine", nullable = false, length = 100)
    private String engine;

    @NotNull(message = "Power is required")
    @Min(value = 1, message = "Power must be greater than 0")
    @Column(name = "power")
    private Integer power;

    @NotNull(message = "Fuel type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 50)
    private FuelType fuelType;

    @NotNull(message = "Transmission type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false, length = 50)
    private TransmissionType transmission;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleCompatibility> vehicleCompatibilities = new ArrayList<>();
}