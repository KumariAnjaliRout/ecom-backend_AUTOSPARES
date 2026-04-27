package com.automobile.ecom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    // ─── ENGINE TYPES ───────────────────────────────────────
    @GetMapping("/engine-types")
    public ResponseEntity<List<String>> getEngineTypes() {
        List<String> engineTypes = List.of(
                "Inline-3",
                "Inline-4",
                "V6",
                "V8",
                "Electric",
                "Hybrid"
        );
        return ResponseEntity.ok(engineTypes);
    }

    // ─── BRAKE TYPES ────────────────────────────────────────
    @GetMapping("/brake-types")
    public ResponseEntity<List<String>> getBrakeTypes() {
        List<String> brakeTypes = List.of(
                "Disc",
                "Drum",
                "Disc + Drum"
        );
        return ResponseEntity.ok(brakeTypes);
    }

    // ─── BRAKE SYSTEMS ──────────────────────────────────────
    @GetMapping("/brake-systems")
    public ResponseEntity<List<String>> getBrakeSystems() {
        List<String> brakeSystems = List.of(
                "ABS",
                "ABS with EBD",
                "ABS + EBD + BA",
                "No ABS"
        );
        return ResponseEntity.ok(brakeSystems);
    }

    // ─── TRANSMISSION TYPES ─────────────────────────────────
    @GetMapping("/transmissions")
    public ResponseEntity<List<String>> getTransmissions() {
        List<String> transmissions = List.of(
                "Manual",
                "Automatic",
                "CVT",
                "AMT"
        );
        return ResponseEntity.ok(transmissions);
    }

    // ─── DRIVE TYPES (CONFIGURATION AXIS) ───────────────────
    @GetMapping("/drive-types")
    public ResponseEntity<List<String>> getDriveTypes() {
        List<String> driveTypes = List.of(
                "FWD",
                "RWD",
                "AWD",
                "4WD"
        );
        return ResponseEntity.ok(driveTypes);
    }

    // ─── MOTOR TYPES (FOR EV) ───────────────────────────────
    @GetMapping("/motor-types")
    public ResponseEntity<List<String>> getMotorTypes() {
        List<String> motorTypes = List.of(
                "Permanent Magnet",
                "Induction Motor"
        );
        return ResponseEntity.ok(motorTypes);
    }

    @GetMapping("/fuel-types")
    public ResponseEntity<List<String>> getFuelTypes() {
        List<String> fuelTypes = List.of(
                "PETROL",
                "DIESEL",
                "ELECTRIC",
                "HYBRID",
                "CNG",
                "LPG"
        );
        return ResponseEntity.ok(fuelTypes);
    }
}
