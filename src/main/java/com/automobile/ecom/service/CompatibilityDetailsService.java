package com.automobile.ecom.service;

import com.automobile.ecom.document.CompatibilityDetailsDocument;
import com.automobile.ecom.document.ProductDocument;
import com.automobile.ecom.dto.CompatibilityDetailsRequestDTO;
import com.automobile.ecom.dto.CompatibilityDetailsResponseDTO;
import com.automobile.ecom.entity.CompatibilityDetails;
import com.automobile.ecom.entity.VehicleCompatibility;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.CompatibilityDetailsRepository;
import com.automobile.ecom.repository.CompatibilityDetailsSearchRepository;
import com.automobile.ecom.repository.VehicleCompatibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompatibilityDetailsService {

    private final CompatibilityDetailsRepository compatibilityDetailsRepository;
    private final VehicleCompatibilityRepository vehicleCompatibilityRepository;
    private final CompatibilityDetailsSearchRepository compatibilityDetailsSearchRepository;

    // ─── CREATE ──────────────────────────────────────────────
    @Transactional
    public CompatibilityDetailsResponseDTO createDetails(Long compatibilityId, CompatibilityDetailsRequestDTO dto) {

        // Use compatibilityId from path variable, not dto
        if (compatibilityDetailsRepository.existsByVehicleCompatibilityId(compatibilityId)) {
            throw new IllegalArgumentException("Compatibility details already exist for compatibility id: "
                    + compatibilityId);
        }

        VehicleCompatibility vehicleCompatibility = vehicleCompatibilityRepository
                .findById(compatibilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle compatibility not found with id: " + compatibilityId));

        CompatibilityDetails details = CompatibilityDetails.builder()
                .vehicleCompatibility(vehicleCompatibility)
                .engineType(dto.getEngineType())
                .engineLiters(dto.getEngineLiters())
                .engineCodes(dto.getEngineCodes())
                .enginePower(dto.getEnginePower())
                .brakeType(dto.getBrakeType())
                .brakeSystem(dto.getBrakeSystem())
                .motorType(dto.getMotorType())
                .volumeOfCcm(dto.getVolumeOfCcm())
                .tank(dto.getTank())
                .abs(dto.getAbs())
                .configurationAxis(dto.getConfigurationAxis())
                .transmissionType(dto.getTransmissionType())
                .build();

        CompatibilityDetails saved = compatibilityDetailsRepository.saveAndFlush(details);

        try {
            CompatibilityDetailsDocument doc = mapToDocument(saved);
            compatibilityDetailsSearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    // ─── GET BY COMPATIBILITY ID ──────────────────────────────
    @Transactional(readOnly = true)
    public CompatibilityDetailsResponseDTO getDetailsByCompatibilityId(Long vehicleCompatibilityId) {
        CompatibilityDetails details = compatibilityDetailsRepository
                .findByVehicleCompatibilityId(vehicleCompatibilityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compatibility details not found for compatibility id: " + vehicleCompatibilityId));
        return mapToResponse(details);
    }

    // ─── GET BY ID ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public CompatibilityDetailsResponseDTO getDetailsById(Long id) {
        return mapToResponse(findDetailsById(id));
    }

    // ─── UPDATE ──────────────────────────────────────────────
    @Transactional
    public CompatibilityDetailsResponseDTO updateDetails(Long id, CompatibilityDetailsRequestDTO dto) {
        CompatibilityDetails details = findDetailsById(id);

        details.setEngineType(dto.getEngineType());
        details.setEngineLiters(dto.getEngineLiters());
        details.setEngineCodes(dto.getEngineCodes());
        details.setEnginePower(dto.getEnginePower());
        details.setBrakeType(dto.getBrakeType());
        details.setBrakeSystem(dto.getBrakeSystem());
        details.setMotorType(dto.getMotorType());
        details.setVolumeOfCcm(dto.getVolumeOfCcm());
        details.setTank(dto.getTank());
        details.setAbs(dto.getAbs());
        details.setConfigurationAxis(dto.getConfigurationAxis());
        details.setTransmissionType(dto.getTransmissionType());
        CompatibilityDetails saved = compatibilityDetailsRepository.save(details);

        // ✅ ES SAVE
        try {
            compatibilityDetailsSearchRepository.save(mapToDocument(saved));
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(compatibilityDetailsRepository.save(details));
    }

    // ─── DELETE ──────────────────────────────────────────────
    @Transactional
    public void deleteDetails(Long id) {
        CompatibilityDetails details = findDetailsById(id);
        try {
            compatibilityDetailsSearchRepository.deleteById(id.toString());
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        compatibilityDetailsRepository.delete(details);
    }

    // ─── HELPER: find or throw ────────────────────────────────
    private CompatibilityDetails findDetailsById(Long id) {
        return compatibilityDetailsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compatibility details not found with id: " + id));
    }

    // ─── HELPER: entity → response DTO ───────────────────────
    private CompatibilityDetailsResponseDTO mapToResponse(CompatibilityDetails details) {
        return CompatibilityDetailsResponseDTO.builder()
                .id(details.getId())
                .engineType(details.getEngineType())
                .engineLiters(details.getEngineLiters())
                .engineCodes(details.getEngineCodes())
                .enginePower(details.getEnginePower())
                .brakeType(details.getBrakeType())
                .brakeSystem(details.getBrakeSystem())
                .motorType(details.getMotorType())
                .volumeOfCcm(details.getVolumeOfCcm())
                .tank(details.getTank())
                .abs(details.getAbs())
                .configurationAxis(details.getConfigurationAxis())
                .transmissionType(details.getTransmissionType())
                .build();
    }
    private CompatibilityDetailsDocument mapToDocument(CompatibilityDetails details) {

        var vc = details.getVehicleCompatibility();
        var product = vc.getProduct();
        var vehicle = vc.getVehicle();

        return CompatibilityDetailsDocument.builder()
                .id(details.getId().toString())

                // 🔹 Core Fields
                .engineType(details.getEngineType())
                .engineLiters(details.getEngineLiters())
                .engineCodes(details.getEngineCodes())
                .enginePower(details.getEnginePower())
                .brakeType(details.getBrakeType())
                .brakeSystem(details.getBrakeSystem())
                .motorType(details.getMotorType())
                .volumeOfCcm(details.getVolumeOfCcm())
                .tank(details.getTank())
                .abs(details.getAbs())
                .configurationAxis(details.getConfigurationAxis())
                .transmissionType(details.getTransmissionType())

                // 🔹 Relations
                .vehicleCompatibilityId(vc.getId().toString())

                .productId(product.getId().toString())
                .productName(product.getName())

                .vehicleId(vehicle.getId().toString())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())

                // 🔹 Metadata
                .createdAt(details.getCreatedAt())

                .build();
    }
}



//package com.automobile.ecom.service;
//
//import com.automobile.ecom.document.CompatibilityDetailsDocument;
//import com.automobile.ecom.document.ProductDocument;
//import com.automobile.ecom.dto.CompatibilityDetailsRequestDTO;
//import com.automobile.ecom.dto.CompatibilityDetailsResponseDTO;
//import com.automobile.ecom.entity.CompatibilityDetails;
//import com.automobile.ecom.entity.VehicleCompatibility;
//import com.automobile.ecom.exception.ResourceNotFoundException;
//import com.automobile.ecom.repository.CompatibilityDetailsRepository;
//import com.automobile.ecom.repository.CompatibilityDetailsSearchRepository;
//import com.automobile.ecom.repository.VehicleCompatibilityRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class CompatibilityDetailsService {
//
//    private final CompatibilityDetailsRepository compatibilityDetailsRepository;
//    private final VehicleCompatibilityRepository vehicleCompatibilityRepository;
//    private final CompatibilityDetailsSearchRepository compatibilityDetailsSearchRepository;
//
//    // ─── CREATE ──────────────────────────────────────────────
//    @Transactional
//    public CompatibilityDetailsResponseDTO createDetails(CompatibilityDetailsRequestDTO dto) {
//
//        // check if details already exist for this compatibility
//        if (compatibilityDetailsRepository.existsByVehicleCompatibilityId(dto.getVehicleCompatibilityId())) {
//            throw new IllegalArgumentException("Compatibility details already exist for compatibility id: "
//                    + dto.getVehicleCompatibilityId());
//        }
//
//        VehicleCompatibility vehicleCompatibility = vehicleCompatibilityRepository
//                .findById(dto.getVehicleCompatibilityId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Vehicle compatibility not found with id: " + dto.getVehicleCompatibilityId()));
//
//        CompatibilityDetails details = CompatibilityDetails.builder()
//                .vehicleCompatibility(vehicleCompatibility)
//                .engineType(dto.getEngineType())
//                .engineLiters(dto.getEngineLiters())
//                .engineCodes(dto.getEngineCodes())
//                .enginePower(dto.getEnginePower())
//                .brakeType(dto.getBrakeType())
//                .brakeSystem(dto.getBrakeSystem())
//                .motorType(dto.getMotorType())
//                .volumeOfCcm(dto.getVolumeOfCcm())
//                .tank(dto.getTank())
//                .abs(dto.getAbs())
//                .configurationAxis(dto.getConfigurationAxis())
//                .transmissionType(dto.getTransmissionType())
//                .build();
//        CompatibilityDetails saved = compatibilityDetailsRepository.save(details);
//
//        // ✅ ES SAVE
//        try {
//            CompatibilityDetailsDocument doc = mapToDocument(saved);
//            compatibilityDetailsSearchRepository.save(doc);
//        } catch (Exception e) {
//            throw new RuntimeException("Elasticsearch sync failed", e);
//        }
//
//        return mapToResponse(saved);
//
////        return mapToResponse(compatibilityDetailsRepository.save(details));
//    }
//
//    // ─── GET BY COMPATIBILITY ID ──────────────────────────────
//    @Transactional(readOnly = true)
//    public CompatibilityDetailsResponseDTO getDetailsByCompatibilityId(Long vehicleCompatibilityId) {
//        CompatibilityDetails details = compatibilityDetailsRepository
//                .findByVehicleCompatibilityId(vehicleCompatibilityId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Compatibility details not found for compatibility id: " + vehicleCompatibilityId));
//        return mapToResponse(details);
//    }
//
//    // ─── GET BY ID ───────────────────────────────────────────
//    @Transactional(readOnly = true)
//    public CompatibilityDetailsResponseDTO getDetailsById(Long id) {
//        return mapToResponse(findDetailsById(id));
//    }
//
//    // ─── UPDATE ──────────────────────────────────────────────
//    @Transactional
//    public CompatibilityDetailsResponseDTO updateDetails(Long id, CompatibilityDetailsRequestDTO dto) {
//        CompatibilityDetails details = findDetailsById(id);
//
//        details.setEngineType(dto.getEngineType());
//        details.setEngineLiters(dto.getEngineLiters());
//        details.setEngineCodes(dto.getEngineCodes());
//        details.setEnginePower(dto.getEnginePower());
//        details.setBrakeType(dto.getBrakeType());
//        details.setBrakeSystem(dto.getBrakeSystem());
//        details.setMotorType(dto.getMotorType());
//        details.setVolumeOfCcm(dto.getVolumeOfCcm());
//        details.setTank(dto.getTank());
//        details.setAbs(dto.getAbs());
//        details.setConfigurationAxis(dto.getConfigurationAxis());
//        details.setTransmissionType(dto.getTransmissionType());
//        CompatibilityDetails saved = compatibilityDetailsRepository.save(details);
//
//        // ✅ ES SAVE
//        try {
//            compatibilityDetailsSearchRepository.save(mapToDocument(saved));
//        } catch (Exception e) {
//            System.out.println("ES sync failed: " + e.getMessage());
//        }
//
//        return mapToResponse(saved);
////        return mapToResponse(compatibilityDetailsRepository.save(details));
//    }
//
//    // ─── DELETE ──────────────────────────────────────────────
//    @Transactional
//    public void deleteDetails(Long id) {
//        CompatibilityDetails details = findDetailsById(id);
//        try {
//            compatibilityDetailsSearchRepository.deleteById(id.toString());
//        } catch (Exception e) {
//            System.out.println("ES sync failed: " + e.getMessage());
//        }
//
//        compatibilityDetailsRepository.delete(details);
//    }
//
//    // ─── HELPER: find or throw ────────────────────────────────
//    private CompatibilityDetails findDetailsById(Long id) {
//        return compatibilityDetailsRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Compatibility details not found with id: " + id));
//    }
//
//    // ─── HELPER: entity → response DTO ───────────────────────
//    private CompatibilityDetailsResponseDTO mapToResponse(CompatibilityDetails details) {
//        return CompatibilityDetailsResponseDTO.builder()
//                .id(details.getId())
//                .engineType(details.getEngineType())
//                .engineLiters(details.getEngineLiters())
//                .engineCodes(details.getEngineCodes())
//                .enginePower(details.getEnginePower())
//                .brakeType(details.getBrakeType())
//                .brakeSystem(details.getBrakeSystem())
//                .motorType(details.getMotorType())
//                .volumeOfCcm(details.getVolumeOfCcm())
//                .tank(details.getTank())
//                .abs(details.getAbs())
//                .configurationAxis(details.getConfigurationAxis())
//                .transmissionType(details.getTransmissionType())
//                .build();
//    }
//    private CompatibilityDetailsDocument mapToDocument(CompatibilityDetails details) {
//
//        var vc = details.getVehicleCompatibility();
//        var product = vc.getProduct();
//        var vehicle = vc.getVehicle();
//
//        return CompatibilityDetailsDocument.builder()
//                .id(details.getId().toString())
//
//                // 🔹 Core Fields
//                .engineType(details.getEngineType())
//                .engineLiters(details.getEngineLiters())
//                .engineCodes(details.getEngineCodes())
//                .enginePower(details.getEnginePower())
//                .brakeType(details.getBrakeType())
//                .brakeSystem(details.getBrakeSystem())
//                .motorType(details.getMotorType())
//                .volumeOfCcm(details.getVolumeOfCcm())
//                .tank(details.getTank())
//                .abs(details.getAbs())
//                .configurationAxis(details.getConfigurationAxis())
//                .transmissionType(details.getTransmissionType())
//
//                // 🔹 Relations
//                .vehicleCompatibilityId(vc.getId().toString())
//
//                .productId(product.getId().toString())
//                .productName(product.getName())
//
//                .vehicleId(vehicle.getId().toString())
//                .brand(vehicle.getBrand())
//                .model(vehicle.getModel())
//                .year(vehicle.getYear())
//
//                // 🔹 Metadata
//                .createdAt(details.getCreatedAt())
//
//                .build();
//    }
//}