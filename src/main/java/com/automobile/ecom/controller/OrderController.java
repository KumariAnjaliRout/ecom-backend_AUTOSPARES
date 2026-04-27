package com.automobile.ecom.controller;


import com.automobile.ecom.dto.*;
import com.automobile.ecom.security.CustomUserPrincipal;
import com.automobile.ecom.security.JwtUtil;
import com.automobile.ecom.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    // ================= ADD TO CART =================
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/bag/add")
    public OrderItemsDTO addToBag(
            @Valid @RequestBody OrderItemsDTO dto,
            @AuthenticationPrincipal CustomUserPrincipal user) {

        log.info("Add to cart");
        return orderService.addToBag(dto, user.getUserId());
    }

    // ================= REMOVE FROM CART =================
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/bag/remove/{orderItemId}")
    public String removeFromBag(
            @PathVariable Long orderItemId,
            @AuthenticationPrincipal CustomUserPrincipal user) {

        log.info("Remove from cart");
        orderService.removeFromBag(orderItemId, user.getUserId());
        return "Removed successfully";
    }

    //clear complete cart
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/cart/clear")
    public String clearCart(@AuthenticationPrincipal CustomUserPrincipal user) {

        orderService.clearCart(user.getUserId());
        return "Cart cleared";
    }

    // ================= UPDATE QUANTITY =================
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/cart/update/{orderItemId}")
    public OrderItemsDTO updateCartItemQuantity(
            @PathVariable Long orderItemId,
            @RequestParam Integer change,
            @AuthenticationPrincipal CustomUserPrincipal user) {

        return orderService.updateCartItemQuantity(orderItemId, change, user.getUserId());
    }

    // ================= PLACE ORDER =================
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place/cod")
    public OrderResponseDTO placeOrder(
            @Valid @RequestBody OrderRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserPrincipal user) {

        log.info("Place order through cod ");
        return orderService.placeOrder(requestDTO, user.getUserId());
    }
//    @PostMapping("/place/online")
//    public OrderResponseDTO placeOnlineOrder(
//            @Valid @RequestBody OrderRequestDTO requestDTO,
//            @AuthenticationPrincipal CustomUserPrincipal user) {
//
//        log.info("Place order through online payment");
//        return orderService.placeOnlineOrder(requestDTO, user.getUserId());
//    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/place/online")
    public OrderResponseDTO placeOnlineOrder(
            @RequestBody(required = false) OrderRequestDTO requestDTO,
            @RequestHeader("Authorization") String token) {

        if (requestDTO == null) {
            throw new RuntimeException("Request body missing");
        }

        UUID userId = jwtUtil.extractUserId(token.substring(7));
        return orderService.placeOnlineOrder(requestDTO, userId);
    }
    // ================= GET ORDER =================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrderById(
            @PathVariable Long orderId) {

        return orderService.getOrderById(orderId);
    }

    // ================= USER ORDERS =================

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/all/user")
    public List<OrderResponseDTO> getOrdersByUser(
            @AuthenticationPrincipal CustomUserPrincipal user) {

        return orderService.getOrdersByUser(user.getUserId());
    }

    // ================= CART =================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/cart")
    public CartResponseDTO getCartItems(
            @AuthenticationPrincipal CustomUserPrincipal user) {

        return orderService.getCartItems(user.getUserId());
    }

    // ================= ADMIN =================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public List<OrderResponseDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/dispatch/{orderId}")
    public OrderResponseDTO dispatchOrder(@PathVariable Long orderId) {
        return orderService.dispatchOrder(orderId);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/deliver/{orderId}")
    public OrderResponseDTO deliverOrder(@PathVariable Long orderId) {
        return orderService.deliverOrder(orderId);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/cancel/{orderId}")
    public OrderResponseDTO cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        return orderService.cancelOrder(orderId, user.getUserId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/total-orders")
    public long totalOrders() {
        return orderService.getTotalOrders();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/recent-orders")
    public List<OrderResponseDTO> recentOrders(
            @RequestParam(defaultValue = "10") int limit) {

        return orderService.getRecentOrders(limit);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/allorders")
    public Page<OrderResponseDTO> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return orderService.getAllOrdersPaginated(page, size);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("dashboard/summary")
    public DashboardSummaryDTO getSummary() {
        return new DashboardSummaryDTO(
                orderService.getTotalOrders(),
                orderService.getTodaySales(),
                orderService.getMonthlySales(),
                orderService.getYearlySales()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("dashboard/top-products")
    public List<TopProductDTO> getTopProducts() {
        return orderService.getTopProducts(5);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("dashboard/sales-trend")
    public List<SalesTrendDTO> getSalesTrend() {
        return orderService.getSalesTrend(7);
    }

}