package com.automobile.ecom.document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Document(indexName = "vehicle_compatibility", writeTypeHint = WriteTypeHint.FALSE)
//@Document(indexName = "vehicle_compatibility")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCompatibilityDocument {

    @Id
    private String id;

    // 🔹 Product Info
    private String productId;
    private String productName;
    private String partNumber;
    private String company;
    private String categoryName;
    private String subCategoryName;

    // 🔹 Vehicle Info
    private String vehicleId;
    private String vehicleBrand;
    private String model;
    private Integer year;
    private String engine;
    private Integer power;
    private String fuelType;
    private String transmission;

    // 🔹 Compatibility Details (if exists)
//    private String engineType;
//    private Double engineLiters;
//    private String engineCodes;
//    private Integer enginePower;
//    private String brakeType;
//    private String brakeSystem;
//    private String motorType;
//    private Integer volumeOfCcm;
//    private Double tank;
//    private Boolean abs;
//    private String configurationAxis;
//    private String transmissionType;

    // 🔹 Metadata
//    private LocalDateTime createdAt;
}