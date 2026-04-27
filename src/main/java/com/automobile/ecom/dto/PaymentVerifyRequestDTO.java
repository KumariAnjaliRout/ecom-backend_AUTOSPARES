package com.automobile.ecom.dto;


import lombok.Data;

@Data
public class PaymentVerifyRequestDTO {
    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
