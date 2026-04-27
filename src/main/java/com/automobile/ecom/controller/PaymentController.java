package com.automobile.ecom.controller;


import com.automobile.ecom.dto.PaymentResponseDTO;
import com.automobile.ecom.dto.PaymentVerifyRequestDTO;
import com.automobile.ecom.security.CustomUserPrincipal;
import com.automobile.ecom.security.JwtUtil;
import com.automobile.ecom.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create/{orderId}")
    public PaymentResponseDTO createPayment(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {

        log.info("Request received to create payment for orderId: {}", orderId);
        UUID uid = jwtUtil.extractUserId(token.substring(7));
        return paymentService.createPayment(orderId, uid);
    }

    @PostMapping("/verify")
    public PaymentResponseDTO verifyPayment(@AuthenticationPrincipal CustomUserPrincipal principal,@RequestBody PaymentVerifyRequestDTO request) {
        log.info("Request received to verify payment for paymentId: {}", request.getPaymentId());
        return paymentService.verifyPayment(principal.getUserId(),request);
    }

    @PutMapping("/failed/{paymentId}")
    public PaymentResponseDTO markPaymentFailed(@PathVariable Long paymentId) {
        log.info("Request received to mark payment failed for paymentId: {}", paymentId);
        return paymentService.markPaymentFailed(paymentId);
    }
}