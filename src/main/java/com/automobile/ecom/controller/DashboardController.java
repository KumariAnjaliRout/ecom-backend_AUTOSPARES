package com.automobile.ecom.controller;

import com.automobile.ecom.dto.ProductResponseDTO;
import com.automobile.ecom.dto.TopCustomerDTO;
import com.automobile.ecom.service.OrderService;
import com.automobile.ecom.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/unsold-products")
    public ResponseEntity<?> getUnsoldProducts() {
        return ResponseEntity.ok(productService.getUnsoldProducts());
    }

    @GetMapping("/product-status")
    public ResponseEntity<?> getProductStatus() {
        return ResponseEntity.ok(productService.getProductStatusStats());
    }

    @GetMapping("/category-status")
    public ResponseEntity<?> getCategoryStatus() {
        return ResponseEntity.ok(productService.getCategoryStats());
    }

    @GetMapping("/subcategory-status")
    public ResponseEntity<?> getSubCategoryStatus() {
        return ResponseEntity.ok(productService.getSubCategoryStats());
    }

    @GetMapping("/payment-status")
    public ResponseEntity<?> getPaymentStatus() {
        return ResponseEntity.ok(orderService.getPaymentStatusStats());
    }

    @GetMapping("/revenue-by-mode")
    public ResponseEntity<?> getRevenueByMode() {
        return ResponseEntity.ok(orderService.getRevenueByPaymentMode());
    }


    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(orderService.getTopCustomers(limit));
    }

}
