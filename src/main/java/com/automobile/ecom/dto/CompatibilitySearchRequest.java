package com.automobile.ecom.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilitySearchRequest {

    // search text

    private String VehicleBrand;
    private String fuelType;
    private Integer year;
    private String model;
    private String query;

    private int page = 0;
    private int size = 10;
}
