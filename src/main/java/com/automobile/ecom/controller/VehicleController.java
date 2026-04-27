package com.automobile.ecom.controller;


import com.automobile.ecom.dto.VehicleRequestDTO;
import com.automobile.ecom.dto.VehicleResponseDTO;
import com.automobile.ecom.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    // ─── CREATE (Admin Only) ───────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponseDTO> createVehicle(@Valid @RequestBody VehicleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleService.createVehicle(dto));
    }

    // ─── GET ALL (Admin Only - For Management Table) ───────────
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VehicleResponseDTO>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    // ─── DROPDOWN 1: All Unique Brands ─────────────────────────
    @GetMapping("/brands")
    public ResponseEntity<Map<String, Object>> getAllBrands() {
        Map<String, Object> response = new LinkedHashMap<>();
        List<String> brands = vehicleService.getAllBrands();
        response.put("totalBrands", brands.size());
        response.put("brands", brands);
        return ResponseEntity.ok(response);
    }

    // ─── DROPDOWN 2: Models by Brand ──────────────────────────
    @GetMapping("/brands/{brand}/models")
    public ResponseEntity<Map<String, Object>> getModelsByBrand(@PathVariable String brand) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brand", brand);
        response.put("models", vehicleService.getModelsByBrand(brand));
        return ResponseEntity.ok(response);
    }

    // ─── DROPDOWN 3: Years by Brand + Model ───────────────────
    @GetMapping("/brands/{brand}/models/{model}/years")
    public ResponseEntity<Map<String, Object>> getYearsByBrandAndModel(
            @PathVariable String brand,
            @PathVariable String model) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("brand", brand);
        response.put("model", model);
        response.put("years", vehicleService.getYearsByBrandAndModel(brand, model));
        return ResponseEntity.ok(response);
    }

    // ─── GET BY ID (Public/User) ──────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    // ─── UPDATE (Admin Only) ───────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDTO dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    // ─── TOGGLE STATUS (Admin Only - Hide/Show) ───────────────
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponseDTO> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.toggleVehicleStatus(id));
    }

    // ─── HARD DELETE (Admin Only - The Mistake Rule) ───────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        // This physically removes the record to fix administrative errors
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}