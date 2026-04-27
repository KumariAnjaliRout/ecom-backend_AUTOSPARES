package com.automobile.ecom.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityDetailsResponseDTO {
    private Long id;
    private String engineType;
    private Double engineLiters;
    private String engineCodes;
    private Integer enginePower;
    private String brakeType;
    private String brakeSystem;
    private String motorType;
    private Integer volumeOfCcm;
    private Double tank;
    private Boolean abs;
    private String configurationAxis;
    private String transmissionType;
    private LocalDateTime createdAt;
}