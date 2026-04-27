package com.automobile.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SalesTrendDTO {
    private LocalDateTime date;
    private Double amount;
}