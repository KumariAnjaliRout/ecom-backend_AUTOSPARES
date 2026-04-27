package com.automobile.ecom.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityDetailsRequestDTO {

    @NotNull(message = "Vehicle Compatibility ID is required")
    private Long vehicleCompatibilityId;

    @NotBlank(message = "Engine type is required")
    private String engineType;

    @NotNull(message = "Engine liters is required")
    @DecimalMin(value = "0.1", message = "Engine liters must be greater than 0")
    private Double engineLiters;

    @NotBlank(message = "Engine codes is required")
    private String engineCodes;

    @NotNull(message = "Engine power is required")
    @Min(value = 1, message = "Engine power must be greater than 0")
    private Integer enginePower;

    @NotBlank(message = "Brake type is required")
    private String brakeType;

    @NotBlank(message = "Brake system is required")
    private String brakeSystem;

    private String motorType;           // ← optional, only for EVs

    @NotNull(message = "Volume in CCM is required")
    @Min(value = 1, message = "Volume must be greater than 0")
    private Integer volumeOfCcm;

    @NotNull(message = "Tank capacity is required")
    @DecimalMin(value = "1.0", message = "Tank must be greater than 0")
    private Double tank;

    @NotNull(message = "ABS info is required")
    private Boolean abs;

    private String configurationAxis;   // ← optional, FWD/RWD/AWD

    @NotBlank(message = "Transmission type is required")
    private String transmissionType;
}