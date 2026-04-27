package com.automobile.ecom.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class TopCustomerDTO {
    private UUID userId;
    private String username;
    private Double totalRevenue;
    private Long totalOrders;
}
