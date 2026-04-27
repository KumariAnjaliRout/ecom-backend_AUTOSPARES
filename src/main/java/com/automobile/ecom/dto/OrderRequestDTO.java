package com.automobile.ecom.dto;

import com.automobile.ecom.entity.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {
    private List<Long> orderItemIds;
    private PaymentMode paymentMode;
}