package com.automobile.ecom.service;


import com.automobile.ecom.dto.PaymentResponseDTO;
import com.automobile.ecom.dto.PaymentVerifyRequestDTO;
import com.automobile.ecom.dto.SendNotificationRequest;
import com.automobile.ecom.entity.*;
import com.automobile.ecom.repository.OrderRepository;
import com.automobile.ecom.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentResponseDTO createPayment(Long orderId, UUID uid) {
        log.info("Creating payment for orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(uid)) {
            throw new RuntimeException("Unauthorized user");
        }

        if (order.getPaymentMode() != PaymentMode.ONLINE) {
            throw new RuntimeException("Selected payment mode is not ONLINE");
        }

        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount", Math.round((order.getTotalAmount() + order.getDeliveryCost()) * 100));
            options.put("currency", "INR");
            options.put("receipt", "order_" + order.getId());

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);

            Payment payment = paymentRepository.findByOrderId(orderId).orElse(new Payment());
            payment.setOrder(order);
            payment.setUid(uid);
            payment.setAmount(order.getTotalAmount());
            payment.setPaymentStatus(PaymentStatus.CREATE);
            payment.setRazorpayOrderId(razorpayOrder.get("id"));

            Payment saved = paymentRepository.save(payment);

            return new PaymentResponseDTO(
                    saved.getId(),
                    saved.getOrder().getId(),
                    saved.getAmount(),
                    saved.getPaymentStatus().name(),
                    saved.getRazorpayOrderId(),
                    saved.getRazorpayPaymentId(),
                    saved.getRazorpaySignature(),
                    razorpayKeyId,
                    "INR"
            );


        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    public PaymentResponseDTO verifyPayment(UUID userId,PaymentVerifyRequestDTO request) {
        log.info("Verifying payment for paymentId: {}", request.getPaymentId());

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        try {
            String generatedSignature = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

            boolean isValid = Utils.verifySignature(
                    generatedSignature,
                    request.getRazorpaySignature(),
                    razorpayKeySecret
            );

            if (isValid) {
                payment.setRazorpayOrderId(request.getRazorpayOrderId());
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
//                payment.getOrder().setOrderStatus(OrderStatus.PLACED);
                try{
                notificationService.sendNotification(
                        SendNotificationRequest.builder()
                                .receiverId(userId)
                                .title("Payment Successful")
                                .category(Category.PAYMENT)
                                .notificationType(NotificationType.PAYMENT_PAID)
                                .message("Your payment was completed successfully via Razorpay.Your order placed successfully")
                                .link("/orders")
                                .notificationChannel(NotificationChannel.BOTH)
                                .build()
                );
            } catch (Exception e) {
                    log.error("Failed to send PAYMENT notification for userId: {}", userId, e);
                }
                try{
                notificationService.sendToAllAdmins(
                        SendNotificationRequest.builder()
                                .senderId(userId)
                                .title("New Order Placed")
                                .category(Category.ORDER)
                                .notificationType(NotificationType.ORDER_PLACED)
                                .message("A new order has been placed successfully via Razorpay.")
                                .link("/orders")
                                .notificationChannel(NotificationChannel.BOTH)
                                .build()
                );
            } catch (Exception e) {
                    log.error("Failed to send notification for placing an order", e);
                }

            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                try{
                notificationService.sendNotification(
                        SendNotificationRequest.builder()
                                .receiverId(userId)
                                .title("Payment Failed")
                                .category(Category.PAYMENT)
                                .notificationType(NotificationType.PAYMENT_FAILED)
                                .message("Your payment could not be completed. Please try again.")
                                .link("/orders")
                                .notificationChannel(NotificationChannel.BOTH)
                                .build()
                );
            }  catch (Exception e) {
                    log.error("Failed to send PAYMENT notification for userId: {}", userId, e);
                }
            }

            Payment saved = paymentRepository.save(payment);

            return new PaymentResponseDTO(
                    saved.getId(),
                    saved.getOrder().getId(),
                    saved.getAmount(),
                    saved.getPaymentStatus().name(),
                    saved.getRazorpayOrderId(),
                    saved.getRazorpayPaymentId(),
                    saved.getRazorpaySignature(),
                    razorpayKeyId,
                    "INR"
            );

        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }

    public PaymentResponseDTO markPaymentFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus(PaymentStatus.FAILED);
        Payment saved = paymentRepository.save(payment);

        return new PaymentResponseDTO(
                saved.getId(),
                saved.getOrder().getId(),
                saved.getAmount(),
                saved.getPaymentStatus().name(),
                saved.getRazorpayOrderId(),
                saved.getRazorpayPaymentId(),
                saved.getRazorpaySignature(),
                razorpayKeyId,
                "INR"
        );
    }
}
