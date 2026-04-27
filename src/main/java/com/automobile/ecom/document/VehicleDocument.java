package com.automobile.ecom.document;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Document(indexName = "vehicle", writeTypeHint = WriteTypeHint.FALSE)
//@Document(indexName = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDocument {

    @Id
    private String id;

    // 🔹 Core Fields
    private String brand;
    private String model;
    private Integer year;
    private String engine;
    private Integer power;

    private String fuelType;
    private String transmission;

    private Boolean isActive;
    private List<String> productIds;
    private List<String> productNames;

    // 🔹 Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}