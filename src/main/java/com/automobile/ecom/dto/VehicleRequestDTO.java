package com.automobile.ecom.dto;



import com.automobile.ecom.entity.FuelType;
import com.automobile.ecom.entity.TransmissionType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequestDTO {
    @NotBlank(message = "Brand is required")
    @Size(min = 2, max = 100, message = "Brand must be between 2 and 100 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 100, message = "Model must be between 1 and 100 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1980, message = "Year must be after 1980")
    @Max(value = 2100, message = "Invalid year")
    private Integer year;

    @NotBlank(message = "Engine is required")
    private String engine;

    @NotNull(message = "Power is required")
    @Min(value = 1, message = "Power must be greater than 0")
    private Integer power;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @NotNull(message = "Transmission is required")
    private TransmissionType transmission;
}