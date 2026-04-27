package com.automobile.ecom.dto;


import com.automobile.ecom.entity.FuelType;
import com.automobile.ecom.entity.TransmissionType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponseDTO {
    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String engine;
    private Integer power;
    private FuelType fuelType;
    private TransmissionType transmission;
    private Boolean isActive;
    private LocalDateTime createdAt;
}