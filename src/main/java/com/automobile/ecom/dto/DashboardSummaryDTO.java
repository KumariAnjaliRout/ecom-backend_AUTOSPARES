package com.automobile.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalOrders;
    private double todaySales;
    private double monthlySales;
    private double yearlySales;
}