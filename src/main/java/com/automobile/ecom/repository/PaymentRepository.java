package com.automobile.ecom.repository;

//package com.example.automobile_ecommerce.repository;
//
//import com.example.automobile_ecommerce.entity.Payment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//import java.util.Optional;
//
//@Repository
//public interface PaymentRepository extends JpaRepository<Payment, Long> {
//    Optional<Payment> findByOrderId(Long orderId);
//}

import com.automobile.ecom.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
}
