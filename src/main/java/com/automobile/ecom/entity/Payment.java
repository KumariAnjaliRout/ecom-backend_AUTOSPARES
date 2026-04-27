package com.automobile.ecom.entity;

//package com.example.automobile_ecommerce.entity;
//
//
//import jakarta.persistence.*;
//import com.example.automobile_ecommerce.entity.Order;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "payments")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Payment {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String uid;
//
//    @OneToOne
//    @JoinColumn(name = "order_id")
//    private Order order;
//
//    private String razorpayPaymentId;
//    private String razorpaySignature;
//
//    private Double amount;
//
//    @Enumerated(EnumType.STRING)
//    private PaymentStatus paymentStatus;
//}

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "payments_search")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uid;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}