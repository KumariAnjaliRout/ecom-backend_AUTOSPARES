package com.automobile.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductDTO {
    private Long productId;
    private String name;
    private Long quantitySold;
}