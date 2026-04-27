package com.automobile.ecom.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private UserResponse user;
    private AddressResponse address;
    private List<OrderItemsDTO> orderItems;
    private Double totalAmount;
    private Double deliveryCost;
    private Double finalAmount;
    private String orderStatus;
    private String paymentMode;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime orderDate;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime cancelledAt;

}

