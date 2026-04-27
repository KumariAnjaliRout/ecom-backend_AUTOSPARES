package com.automobile.ecom.service;

import com.automobile.ecom.document.VehicleDocument;
import com.automobile.ecom.dto.VehicleRequestDTO;
import com.automobile.ecom.dto.VehicleResponseDTO;
import com.automobile.ecom.entity.Vehicle;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.VehicleRepository;
import com.automobile.ecom.repository.VehicleSearchRepository;
import com.automobile.ecom.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleSearchRepository vehicleSearchRepository;
    private final AuditService auditService;
    // ─── CREATE ──────────────────────────────────────────────
    @Transactional
    public VehicleResponseDTO createVehicle(VehicleRequestDTO dto) {

        String cleanBrand = dto.getBrand().trim();
        String cleanModel = dto.getModel().trim();
        String cleanEngine = dto.getEngine().trim();

        if (vehicleRepository.existsByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndEngineIgnoreCaseAndFuelType(
                cleanBrand, cleanModel, dto.getYear(), cleanEngine, dto.getFuelType())) {

            throw new BadRequestException("A vehicle with this configuration already exists.");
        }

        Vehicle vehicle = Vehicle.builder()
                .brand(cleanBrand)
                .model(cleanModel)
                .year(dto.getYear())
                .engine(cleanEngine)
                .power(dto.getPower())
                .fuelType(dto.getFuelType())
                .transmission(dto.getTransmission())
                .isActive(true)
                .build();
        Vehicle saved = vehicleRepository.save(vehicle);
        CustomUserPrincipal user = getCurrentUser();
        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "VEHICLE_CREATED",
                "VEHICLE",
                String.valueOf(vehicle.getId()),
                "Created vehicle: " + saved.getBrand() + " " + saved.getModel()
        );

        try {
            VehicleDocument doc = mapToDocument(saved);
            vehicleSearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(vehicleRepository.save(vehicle));
    }

    // ─── GET ALL (Admin view) ────────────────────────────────
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAllVehicles() {
        // 1. Use a sorted repository call or sort the stream
        return vehicleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Vehicle::getBrand)
                        .thenComparing(Vehicle::getModel))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleById(Long id) {
        // findVehicleById already throws ResourceNotFoundException if missing
        Vehicle vehicle = findVehicleById(id);
        return mapToResponse(vehicle);
    }

    // ─── SEARCH: BY BRAND (User view) ────────────────────────
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getVehiclesByBrand(String brand) {
        // 1. Check for empty or blank input
        if (brand == null || brand.isBlank()) {
            return List.of();
        }

        // 2. Trim the brand to remove accidental spaces
        return vehicleRepository.findAllByBrandIgnoreCaseAndIsActiveTrue(brand.trim())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── DROPDOWN LOGIC ──────────────────────────────────────
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        return vehicleRepository.findAllDistinctBrands();
    }

    @Transactional(readOnly = true)
    public List<String> getModelsByBrand(String brand) {
        if (brand == null || brand.isBlank()) return List.of();

        return vehicleRepository.findAllModelsByBrand(brand.trim());
    }

    @Transactional(readOnly = true)
    public List<Integer> getYearsByBrandAndModel(String brand, String model) {
        if (brand == null || brand.isBlank() || model == null || model.isBlank()) {
            return List.of();
        }

        return vehicleRepository.findAllYearsByBrandAndModel(brand.trim(), model.trim());
    }
    // ─── UPDATE ──────────────────────────────────────────────
    @Transactional
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO dto) {

        Vehicle vehicle = findVehicleById(id);

        String cleanBrand = dto.getBrand().trim();
        String cleanModel = dto.getModel().trim();
        String cleanEngine = dto.getEngine().trim();

        boolean identityChanged =
                !vehicle.getBrand().equalsIgnoreCase(cleanBrand) ||
                        !vehicle.getModel().equalsIgnoreCase(cleanModel) ||
                        !vehicle.getYear().equals(dto.getYear()) ||
                        !vehicle.getEngine().equalsIgnoreCase(cleanEngine) ||
                        !vehicle.getFuelType().equals(dto.getFuelType());

        if (identityChanged &&
                vehicleRepository.existsByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndEngineIgnoreCaseAndFuelType(
                        cleanBrand, cleanModel, dto.getYear(), cleanEngine, dto.getFuelType())) {

            throw new BadRequestException("Another vehicle with this configuration already exists.");
        }

        vehicle.setBrand(cleanBrand);
        vehicle.setModel(cleanModel);
        vehicle.setYear(dto.getYear());
        vehicle.setEngine(cleanEngine);
        vehicle.setPower(dto.getPower());
        vehicle.setFuelType(dto.getFuelType());
        vehicle.setTransmission(dto.getTransmission());
        Vehicle saved = vehicleRepository.save(vehicle);
        CustomUserPrincipal user = getCurrentUser();
        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "VEHICLE_UPDATED",
                "VEHICLE",
                String.valueOf(vehicle.getId()),
                "Updated vehicle"
        );

        try {
            VehicleDocument doc = mapToDocument(saved);
            vehicleSearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(vehicleRepository.save(vehicle));
    }

    // ─── TOGGLE STATUS (Soft Hide) ───────────────────────────
    @Transactional
    public VehicleResponseDTO toggleVehicleStatus(Long id) {
        Vehicle vehicle = findVehicleById(id);
        boolean newStatus = !vehicle.getIsActive();

        vehicle.setIsActive(newStatus);

        // Logic: If the vehicle is hidden, it will automatically
        // stop appearing in the cascading dropdowns because of
        // the "v.isActive = true" checks in your Repository.
        Vehicle saved = vehicleRepository.save(vehicle);

        try {
            VehicleDocument doc = mapToDocument(saved);
            vehicleSearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(vehicleRepository.save(vehicle));
    }

    // ─── HARD DELETE (The Mistake Rule) ──────────────────────
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = findVehicleById(id);

        // 1. Safety Guard: Check if products are already linked to this vehicle
        if (vehicle.getVehicleCompatibilities() != null && !vehicle.getVehicleCompatibilities().isEmpty()) {
            throw new BadRequestException("Cannot delete vehicle: It is currently linked to products. Deactivate it instead.");
        }
        try {
            vehicleSearchRepository.deleteById(id.toString());
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
        // 2. Physical removal
        CustomUserPrincipal user = getCurrentUser();
        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "VEHICLE_DELETED",
                "VEHICLE",
                String.valueOf(vehicle.getId()),
                "Deleted vehicle: " + vehicle.getBrand() + " " + vehicle.getModel()
        );
        vehicleRepository.delete(vehicle);
    }

    // ─── HELPERS ─────────────────────────────────────────────
    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    private CustomUserPrincipal getCurrentUser() {
        return (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private VehicleResponseDTO mapToResponse(Vehicle vehicle) {
        return VehicleResponseDTO.builder()
                .id(vehicle.getId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .engine(vehicle.getEngine())
                .power(vehicle.getPower())
                .fuelType(vehicle.getFuelType())
                .transmission(vehicle.getTransmission())
                .isActive(vehicle.getIsActive())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
    private VehicleDocument mapToDocument(Vehicle vehicle) {

        List<String> productIds = vehicle.getVehicleCompatibilities() == null
                ? List.of()
                : vehicle.getVehicleCompatibilities().stream()
                .map(vc -> vc.getProduct().getId().toString())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> productNames = vehicle.getVehicleCompatibilities() == null
                ? List.of()
                : vehicle.getVehicleCompatibilities().stream()
                .map(vc -> vc.getProduct().getName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return VehicleDocument.builder()
                .id(vehicle.getId().toString())

                // 🔹 Core Fields
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .engine(vehicle.getEngine())
                .power(vehicle.getPower())

                .fuelType(vehicle.getFuelType().name())
                .transmission(vehicle.getTransmission().name())

                .isActive(vehicle.getIsActive())

                // 🔹 Reverse Mapping
                .productIds(productIds)
                .productNames(productNames)

                // 🔹 Metadata
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())

                .build();
    }
}