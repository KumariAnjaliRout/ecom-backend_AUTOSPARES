package com.automobile.ecom.controller;

import com.automobile.ecom.dto.CompatibilityDetailsRequestDTO;
import com.automobile.ecom.dto.CompatibilityDetailsResponseDTO;
import com.automobile.ecom.service.CompatibilityDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compatibility")
@RequiredArgsConstructor
public class CompatibilityDetailsController {

    private final CompatibilityDetailsService compatibilityDetailsService;

    // ─── CREATE (admin) ───────────────────────────────────────
    // POST /api/compatibility/{compatibilityId}/details
    @PostMapping("/{compatibilityId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompatibilityDetailsResponseDTO> createDetails(
            @PathVariable Long compatibilityId,
            @Valid @RequestBody CompatibilityDetailsRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compatibilityDetailsService.createDetails(compatibilityId, dto));
    }
    // ─── GET BY COMPATIBILITY ID ──────────────────────────────
    // GET /api/compatibility/{compatibilityId}/details
    @GetMapping("/{compatibilityId}/details")
    public ResponseEntity<CompatibilityDetailsResponseDTO> getDetailsByCompatibilityId(
            @PathVariable Long compatibilityId) {
        return ResponseEntity.ok(
                compatibilityDetailsService.getDetailsByCompatibilityId(compatibilityId));
    }

    // ─── GET BY DETAILS ID ────────────────────────────────────
    // GET /api/compatibility/details/{id}
    @GetMapping("/details/{id}")
    public ResponseEntity<CompatibilityDetailsResponseDTO> getDetailsById(
            @PathVariable Long id) {
        return ResponseEntity.ok(compatibilityDetailsService.getDetailsById(id));
    }

    // ─── UPDATE ──────────────────────────────────────────────
    // PUT /api/compatibility/details/{id}
    @PutMapping("/details/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompatibilityDetailsResponseDTO> updateDetails(
            @PathVariable Long id,
            @Valid @RequestBody CompatibilityDetailsRequestDTO dto) {
        return ResponseEntity.ok(compatibilityDetailsService.updateDetails(id, dto));
    }

    // ─── DELETE ──────────────────────────────────────────────
    // DELETE /api/compatibility/details/{id}
    @DeleteMapping("/details/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDetails(@PathVariable Long id) {
        compatibilityDetailsService.deleteDetails(id);
        return ResponseEntity.noContent().build();
    }
}



//package com.automobile.ecom.controller;
//
//import com.automobile.ecom.dto.CompatibilityDetailsRequestDTO;
//import com.automobile.ecom.dto.CompatibilityDetailsResponseDTO;
//import com.automobile.ecom.service.CompatibilityDetailsService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/compatibility")
//@RequiredArgsConstructor
//public class CompatibilityDetailsController {
//
//    private final CompatibilityDetailsService compatibilityDetailsService;
//
//    // ─── CREATE (admin) ───────────────────────────────────────
//    // POST /api/compatibility/{compatibilityId}/details
//    @PostMapping("/{compatibilityId}/details")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<CompatibilityDetailsResponseDTO> createDetails(
//            @PathVariable Long compatibilityId,
//            @Valid @RequestBody CompatibilityDetailsRequestDTO dto) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(compatibilityDetailsService.createDetails( dto));
//    }
//    // ─── GET BY COMPATIBILITY ID ──────────────────────────────
//    // GET /api/compatibility/{compatibilityId}/details
//    @GetMapping("/{compatibilityId}/details")
//    public ResponseEntity<CompatibilityDetailsResponseDTO> getDetailsByCompatibilityId(
//            @PathVariable Long compatibilityId) {
//        return ResponseEntity.ok(
//                compatibilityDetailsService.getDetailsByCompatibilityId(compatibilityId));
//    }
//
//    // ─── GET BY DETAILS ID ────────────────────────────────────
//    // GET /api/compatibility/details/{id}
//    @GetMapping("/details/{id}")
//    public ResponseEntity<CompatibilityDetailsResponseDTO> getDetailsById(
//            @PathVariable Long id) {
//        return ResponseEntity.ok(compatibilityDetailsService.getDetailsById(id));
//    }
//
//    // ─── UPDATE ──────────────────────────────────────────────
//    // PUT /api/compatibility/details/{id}
//    @PutMapping("/details/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<CompatibilityDetailsResponseDTO> updateDetails(
//            @PathVariable Long id,
//            @Valid @RequestBody CompatibilityDetailsRequestDTO dto) {
//        return ResponseEntity.ok(compatibilityDetailsService.updateDetails(id, dto));
//    }
//
//    // ─── DELETE ──────────────────────────────────────────────
//    // DELETE /api/compatibility/details/{id}
//    @DeleteMapping("/details/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> deleteDetails(@PathVariable Long id) {
//        compatibilityDetailsService.deleteDetails(id);
//        return ResponseEntity.noContent().build();
//    }
//}