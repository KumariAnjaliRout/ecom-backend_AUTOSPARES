package com.automobile.ecom.controller;


import com.automobile.ecom.service.CompatibilitySearchService;
import com.automobile.ecom.service.VehicleCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compatibility")
@RequiredArgsConstructor
public class CompatibilitySearchController {

    private final CompatibilitySearchService searchService;
    private final VehicleCompatibilityService service;

    @GetMapping("/filter/productsearch")
    public ResponseEntity<?> getProducts(
            @RequestParam String vehicleBrand,
            @RequestParam String fuelType,
            @RequestParam Integer year,
            @RequestParam String model,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                searchService.search(vehicleBrand, fuelType, year, model,query, page, size)
        );
    }



    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public String reindex() {
        service.reindexAllData();
        return "Reindex completed";
    }
}
