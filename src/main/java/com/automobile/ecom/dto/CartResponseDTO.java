package com.automobile.ecom.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private List<OrderItemsDTO> cartItems;
    private Double subTotal;
    private Double deliveryCharge;
    private Double grandTotal;
}
