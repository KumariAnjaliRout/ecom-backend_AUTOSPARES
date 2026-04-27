package com.automobile.ecom.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "compatibility_detail_search")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Engine type is required")
    @Column(name = "engine_type", nullable = false, length = 100)
    private String engineType;

    @NotNull(message = "Engine liters is required")
    @DecimalMin(value = "0.1", message = "Engine liters must be greater than 0")
    @Column(name = "engine_liters", nullable = false)
    private Double engineLiters;

    @NotBlank(message = "Engine code is required")
    @Column(name = "engine_codes", nullable = false, length = 100)
    private String engineCodes;

    @NotNull(message = "Engine power is required")
    @Min(value = 1, message = "Engine power must be greater than 0")
    @Column(name = "engine_power", nullable = false)
    private Integer enginePower;

    @NotBlank(message = "Brake type is required")
    @Column(name = "brake_type", nullable = false, length = 100)
    private String brakeType;

    @NotBlank(message = "Brake system is required")
    @Column(name = "brake_system", nullable = false, length = 100)
    private String brakeSystem;

    @Column(name = "motor_type", length = 100)
    private String motorType;

    @NotNull(message = "Volume in CCM is required")
    @Min(value = 1, message = "Volume must be greater than 0")
    @Column(name = "volume_of_ccm", nullable = false)
    private Integer volumeOfCcm;

    @NotNull(message = "Tank capacity is required")
    @DecimalMin(value = "1.0", message = "Tank capacity must be greater than 0")
    @Column(name = "tank", nullable = false)
    private Double tank;

    @NotNull(message = "ABS info is required")
    @Column(name = "abs", nullable = false)
    private Boolean abs;

    @Column(name = "configuration_axis", length = 100)
    private String configurationAxis;

    @NotBlank(message = "Transmission type is required")
    @Column(name = "transmission_type", nullable = false, length = 100)
    private String transmissionType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "vehicle_compatibility_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_details_compatibility")
    )
    @NotNull(message = "Vehicle compatibility is required")
    private VehicleCompatibility vehicleCompatibility;
}