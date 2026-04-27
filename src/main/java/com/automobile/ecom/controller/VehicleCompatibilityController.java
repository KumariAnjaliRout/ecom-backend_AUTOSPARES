package com.automobile.ecom.controller;


import com.automobile.ecom.dto.VehicleCompatibilityRequestDTO;
import com.automobile.ecom.dto.VehicleCompatibilityResponseDTO;
import com.automobile.ecom.service.VehicleCompatibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compatibility")
@RequiredArgsConstructor
public class VehicleCompatibilityController {

    private final VehicleCompatibilityService vehicleCompatibilityService;

    // ─── CREATE (admin) ───────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleCompatibilityResponseDTO> createCompatibility(
            @Valid @RequestBody VehicleCompatibilityRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleCompatibilityService.createCompatibility(dto));
    }

    // ─── GET BY ID ────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<VehicleCompatibilityResponseDTO> getCompatibilityById(
            @PathVariable Long id) {
        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibilityById(id));
    }

    // ─── GET ALL BY PRODUCT ───────────────────────────────────
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<VehicleCompatibilityResponseDTO>> getCompatibilitiesByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibilitiesByProduct(productId));
    }

    // ─── GET ALL BY VEHICLE ───────────────────────────────────
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<VehicleCompatibilityResponseDTO>> getCompatibilitiesByVehicle(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibilitiesByVehicle(vehicleId));
    }

    // ─── SEARCH COMPATIBLE PRODUCTS BY BRAND/MODEL/YEAR ──────
    @GetMapping("/search")
    public ResponseEntity<List<VehicleCompatibilityResponseDTO>> getCompatibleProducts(
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Integer year) {
        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibleProducts(brand, model, year));
    }

    // ─── DELETE (admin) ───────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompatibility(@PathVariable Long id) {
        vehicleCompatibilityService.deleteCompatibility(id);
        return ResponseEntity.noContent().build();
    }

    // ════════════════════════════════════════════════════════════
    // ─── FILTER DROPDOWN ENDPOINTS ───────────────────────────
    // ════════════════════════════════════════════════════════════

    // ─── STEP 1: GET all brands ───────────────────────────────
    @GetMapping("/filter/brands")
    public ResponseEntity<Map<String, Object>> getFilterBrands() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brands", vehicleCompatibilityService.getFilterBrands());
        return ResponseEntity.ok(response);
    }

    // ─── STEP 2: GET fuel types by brand ─────────────────────
    @GetMapping("/filter/fuel-types")
    public ResponseEntity<Map<String, Object>> getFilterFuelTypes(
            @RequestParam String brand) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brand", brand);
        response.put("fuelTypes", vehicleCompatibilityService.getFilterFuelTypes(brand));
        return ResponseEntity.ok(response);

    }

    // ─── STEP 3: GET years by brand + fuelType ───────────────
    @GetMapping("/filter/years")
    public ResponseEntity<Map<String, Object>> getFilterYears(
            @RequestParam String brand,
            @RequestParam String fuelType) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brand", brand);
        response.put("fuelType", fuelType);
        response.put("years", vehicleCompatibilityService.getFilterYears(brand, fuelType));
        return ResponseEntity.ok(response);
    }

    // ─── STEP 4: GET models by brand + fuelType + year ───────
    @GetMapping("/filter/models")
    public ResponseEntity<Map<String, Object>> getFilterModels(
            @RequestParam String brand,
            @RequestParam String fuelType,
            @RequestParam Integer year) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brand", brand);
        response.put("fuelType", fuelType);
        response.put("year", year);
        response.put("models", vehicleCompatibilityService.getFilterModels(brand, fuelType, year));
        return ResponseEntity.ok(response);
    }

    // ─── STEP 5: GET engines by brand + fuelType + year + model ───
    @GetMapping("/filter/engines")
    public ResponseEntity<Map<String, Object>> getFilterEngines(
            @RequestParam String brand,
            @RequestParam String fuelType,
            @RequestParam Integer year,
            @RequestParam String model) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brand", brand);
        response.put("fuelType", fuelType);
        response.put("year", year);
        response.put("model", model);
        response.put("engines", vehicleCompatibilityService.getFilterEngines(brand, fuelType, year, model));
        return ResponseEntity.ok(response);
    }

    // ─── FINAL: GET products by all filters (including Engine) ───
    @GetMapping("/filter/products")
    public ResponseEntity<Map<String, Object>> getFilteredProducts(
            @RequestParam String brand,
            @RequestParam String fuelType,
            @RequestParam Integer year,
            @RequestParam String model,
            @RequestParam String engine) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brand", brand);
        response.put("fuelType", fuelType);
        response.put("year", year);
        response.put("model", model);
        response.put("engine", engine);
        response.put("products", vehicleCompatibilityService.getFilteredProducts(brand, fuelType, year, model, engine));
        return ResponseEntity.ok(response);
    }
}


//package com.automobile.ecom.controller;
//
//
//import com.automobile.ecom.dto.VehicleCompatibilityRequestDTO;
//import com.automobile.ecom.dto.VehicleCompatibilityResponseDTO;
//import com.automobile.ecom.service.VehicleCompatibilityService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/compatibility")
//@RequiredArgsConstructor
//public class VehicleCompatibilityController {
//
//    private final VehicleCompatibilityService vehicleCompatibilityService;
//
//    // ─── CREATE (admin) ───────────────────────────────────────
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<VehicleCompatibilityResponseDTO> createCompatibility(
//            @Valid @RequestBody VehicleCompatibilityRequestDTO dto) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(vehicleCompatibilityService.createCompatibility(dto));
//    }
//
//    // ─── GET BY ID ────────────────────────────────────────────
//    @GetMapping("/{id}")
//    public ResponseEntity<VehicleCompatibilityResponseDTO> getCompatibilityById(
//            @PathVariable Long id) {
//        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibilityById(id));
//    }
//
//    // ─── GET ALL BY PRODUCT ───────────────────────────────────
//    @GetMapping("/product/{productId}")
//    public ResponseEntity<List<VehicleCompatibilityResponseDTO>> getCompatibilitiesByProduct(
//            @PathVariable Long productId) {
//        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibilitiesByProduct(productId));
//    }
//
//    // ─── GET ALL BY VEHICLE ───────────────────────────────────
//    @GetMapping("/vehicle/{vehicleId}")
//    public ResponseEntity<List<VehicleCompatibilityResponseDTO>> getCompatibilitiesByVehicle(
//            @PathVariable Long vehicleId) {
//        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibilitiesByVehicle(vehicleId));
//    }
//
//    // ─── SEARCH COMPATIBLE PRODUCTS BY BRAND/MODEL/YEAR ──────
//    @GetMapping("/search")
//    public ResponseEntity<List<VehicleCompatibilityResponseDTO>> getCompatibleProducts(
//            @RequestParam String brand,
//            @RequestParam String model,
//            @RequestParam Integer year) {
//        return ResponseEntity.ok(vehicleCompatibilityService.getCompatibleProducts(brand, model, year));
//    }
//
//    // ─── DELETE (admin) ───────────────────────────────────────
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> deleteCompatibility(@PathVariable Long id) {
//        vehicleCompatibilityService.deleteCompatibility(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // ════════════════════════════════════════════════════════════
//    // ─── FILTER DROPDOWN ENDPOINTS ───────────────────────────
//    // ════════════════════════════════════════════════════════════
//
//    // ─── STEP 1: GET all brands ───────────────────────────────
//    @GetMapping("/filter/brands")
//    public ResponseEntity<Map<String, Object>> getFilterBrands() {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("brands", vehicleCompatibilityService.getFilterBrands());
//        return ResponseEntity.ok(response);
//    }
//
//    // ─── STEP 2: GET fuel types by brand ─────────────────────
//    @GetMapping("/filter/fuel-types")
//    public ResponseEntity<Map<String, Object>> getFilterFuelTypes(
//            @RequestParam String brand) {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("brand", brand);
//        response.put("fuelTypes", vehicleCompatibilityService.getFilterFuelTypes(brand));
//        return ResponseEntity.ok(response);
//    }
//
//    // ─── STEP 3: GET years by brand + fuelType ───────────────
//    @GetMapping("/filter/years")
//    public ResponseEntity<Map<String, Object>> getFilterYears(
//            @RequestParam String brand,
//            @RequestParam String fuelType) {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("brand", brand);
//        response.put("fuelType", fuelType);
//        response.put("years", vehicleCompatibilityService.getFilterYears(brand, fuelType));
//        return ResponseEntity.ok(response);
//    }
//
//    // ─── STEP 4: GET models by brand + fuelType + year ───────
//    @GetMapping("/filter/models")
//    public ResponseEntity<Map<String, Object>> getFilterModels(
//            @RequestParam String brand,
//            @RequestParam String fuelType,
//            @RequestParam Integer year) {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("brand", brand);
//        response.put("fuelType", fuelType);
//        response.put("year", year);
//        response.put("models", vehicleCompatibilityService.getFilterModels(brand, fuelType, year));
//        return ResponseEntity.ok(response);
//    }
//
//    // ─── STEP 5: GET engines by brand + fuelType + year + model ───
//    @GetMapping("/filter/engines")
//    public ResponseEntity<Map<String, Object>> getFilterEngines(
//            @RequestParam String brand,
//            @RequestParam String fuelType,
//            @RequestParam Integer year,
//            @RequestParam String model) {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("brand", brand);
//        response.put("fuelType", fuelType);
//        response.put("year", year);
//        response.put("model", model);
//        response.put("engines", vehicleCompatibilityService.getFilterEngines(brand, fuelType, year, model));
//        return ResponseEntity.ok(response);
//    }
//
//    // ─── FINAL: GET products by all filters (including Engine) ───
//    @GetMapping("/filter/products")
//    public ResponseEntity<Map<String, Object>> getFilteredProducts(
//            @RequestParam String brand,
//            @RequestParam String fuelType,
//            @RequestParam Integer year,
//            @RequestParam String model,
//            @RequestParam String engine) {
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("brand", brand);
//        response.put("fuelType", fuelType);
//        response.put("year", year);
//        response.put("model", model);
//        response.put("engine", engine);
//        response.put("products", vehicleCompatibilityService.getFilteredProducts(brand, fuelType, year, model, engine));
//        return ResponseEntity.ok(response);
//    }
//}