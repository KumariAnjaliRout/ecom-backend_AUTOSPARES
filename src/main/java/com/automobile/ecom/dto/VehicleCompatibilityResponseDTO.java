package com.automobile.ecom.dto;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCompatibilityResponseDTO {
    private Long id;

    // Product info
    private Long productId;
    private String productName;

    // Vehicle info
    private Long vehicleId;
    private String vehicleBrand;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleEngine;
    private Integer vehiclePower;
    private String vehicleFuelType;
    private String vehicleTransmission;

    private CompatibilityDetailsResponseDTO compatibilityDetails;
    private LocalDateTime createdAt;
}
