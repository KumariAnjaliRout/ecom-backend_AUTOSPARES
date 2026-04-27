package com.automobile.ecom.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemsDTO {

    private Long id;

    private Long productId;

    private String pname;

    private Double actualPrice;
    private Double discount;
    private Double price;

    private Integer quantity;

    private Double unitPrice;
    private Double totalPrice;
}