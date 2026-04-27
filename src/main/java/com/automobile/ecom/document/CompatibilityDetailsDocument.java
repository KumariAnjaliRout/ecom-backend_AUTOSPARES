package com.automobile.ecom.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Document(indexName = "compatibility_detail", writeTypeHint = WriteTypeHint.FALSE)
//@Document(indexName = "compatibility_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityDetailsDocument {

    @Id
    private String id;

    // 🔹 Core Fields
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

    // 🔹 Relation Info (VERY IMPORTANT 🔥)
    private String vehicleCompatibilityId;

    private String productId;
    private String productName;

    private String vehicleId;
    private String brand;
    private String model;
    private Integer year;

    // 🔹 Metadata
    private LocalDateTime createdAt;
}