package com.automobile.ecom.service;

import com.automobile.ecom.document.VehicleCompatibilityDocument;
import com.automobile.ecom.dto.CompatibilityDetailsResponseDTO;
import com.automobile.ecom.dto.ProductResponseDTO;
import com.automobile.ecom.dto.VehicleCompatibilityRequestDTO;
import com.automobile.ecom.dto.VehicleCompatibilityResponseDTO;
import com.automobile.ecom.entity.CompatibilityDetails;
import com.automobile.ecom.entity.FuelType;
import com.automobile.ecom.entity.Product;
import com.automobile.ecom.entity.Vehicle;
import com.automobile.ecom.entity.VehicleCompatibility;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.ProductRepository;
import com.automobile.ecom.repository.VehicleCompatibilityRepository;
import com.automobile.ecom.repository.VehicleCompatibilitySearchRepository;
import com.automobile.ecom.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleCompatibilityService {

    private final VehicleCompatibilityRepository vehicleCompatibilityRepository;
    private final ProductRepository productRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleCompatibilitySearchRepository vehicleCompatibilitySearchRepository;

    // ─── CREATE ──────────────────────────────────────────────
    @Transactional
    public VehicleCompatibilityResponseDTO createCompatibility(VehicleCompatibilityRequestDTO dto) {
        if (vehicleCompatibilityRepository.existsByProductIdAndVehicleId(
                dto.getProductId(), dto.getVehicleId())) {
            throw new IllegalArgumentException("Compatibility already exists for this product and vehicle");
        }

        Product product = productRepository.findByIdAndIsActiveTrue(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + dto.getProductId()));

        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle not found with id: " + dto.getVehicleId()));

        VehicleCompatibility compatibility = VehicleCompatibility.builder()
                .product(product)
                .vehicle(vehicle)
                .build();

        VehicleCompatibility saved = vehicleCompatibilityRepository.save(compatibility);

        try {
            VehicleCompatibilityDocument doc = mapToDocument(saved);
            vehicleCompatibilitySearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    // ─── GET ALL BY PRODUCT ───────────────────────────────────
    @Transactional(readOnly = true)
    public List<VehicleCompatibilityResponseDTO> getCompatibilitiesByProduct(Long productId) {
        return vehicleCompatibilityRepository.findAllByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET ALL PRODUCTS BY VEHICLE ──────────────────────────
    @Transactional(readOnly = true)
    public List<VehicleCompatibilityResponseDTO> getCompatibilitiesByVehicle(Long vehicleId) {
        return vehicleCompatibilityRepository.findAllProductsByVehicleId(vehicleId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET COMPATIBLE PRODUCTS BY BRAND/MODEL/YEAR ─────────
    @Transactional(readOnly = true)
    public List<VehicleCompatibilityResponseDTO> getCompatibleProducts(
            String brand, String model, Integer year) {
        return vehicleCompatibilityRepository.findCompatibleProducts(brand, model, year)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public VehicleCompatibilityResponseDTO getCompatibilityById(Long id) {
        return mapToResponse(findCompatibilityById(id));
    }

    // ─── DELETE ──────────────────────────────────────────────
    @Transactional
    public void deleteCompatibility(Long id) {
        VehicleCompatibility compatibility = findCompatibilityById(id);
        try {
            vehicleCompatibilitySearchRepository.deleteById(id.toString());
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
        vehicleCompatibilityRepository.delete(compatibility);
    }

    // ════════════════════════════════════════════════════════════
    // ─── FILTER DROPDOWN METHODS ─────────────────────────────
    // ════════════════════════════════════════════════════════════

    // ─── STEP 1: all brands ───────────────────────────────────
    @Transactional(readOnly = true)
    public List<String> getFilterBrands() {
        return vehicleCompatibilityRepository.findDistinctBrands();
    }

    // ─── STEP 2: fuel types by brand ─────────────────────────
    @Transactional(readOnly = true)
    public List<String> getFilterFuelTypes(String brand) {
        return vehicleCompatibilityRepository.findDistinctFuelTypesByBrand(brand)
                .stream()
                .map(FuelType::name)       // ✅ FuelType enum → String
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getFilterYears(String brand, String fuelType) {

        FuelType fuelTypeEnum;
        try {
            fuelTypeEnum = FuelType.valueOf(fuelType.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid fuel type: " + fuelType);
        }

        return vehicleCompatibilityRepository
                .findDistinctYearsByBrandAndFuelType(brand.trim(), fuelTypeEnum);
    }

    // ─── STEP 4: models by brand + fuelType + year ───────────
    @Transactional(readOnly = true)
    public List<String> getFilterModels(String brand, String fuelType, Integer year) {
        return vehicleCompatibilityRepository
                .findDistinctModelsByBrandFuelTypeAndYear(
                        brand,
                        FuelType.valueOf(fuelType.trim().toUpperCase()),   // ✅ String → FuelType
                        year);
    }

    // ─── STEP 5: engines by brand + fuelType + year + model ──
    @Transactional(readOnly = true)
    public List<String> getFilterEngines(String brand, String fuelType, Integer year, String model) {
        return vehicleCompatibilityRepository
                .findProductsByFilter(
                        brand,
                        FuelType.valueOf(fuelType.trim().toUpperCase()),   // ✅ String → FuelType
                        year,
                        model)
                .stream()
                .map(vc -> vc.getVehicle().getEngine())
                .distinct()
                .collect(Collectors.toList());
    }

    // ─── FINAL: products by all filters ──────────────────────
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getFilteredProducts(
            String brand, String fuelType, Integer year, String model, String engine) {
        return vehicleCompatibilityRepository
                .findProductsWithEngine(
                        brand,
                        FuelType.valueOf(fuelType.trim().toUpperCase()),   // ✅ String → FuelType
                        year,
                        model,
                        engine)
                .stream()
                .map(vc -> mapProductToResponse(vc.getProduct()))
                .collect(Collectors.toList());
    }

    // ─── HELPER: find or throw ────────────────────────────────
    private VehicleCompatibility findCompatibilityById(Long id) {
        return vehicleCompatibilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle compatibility not found with id: " + id));
    }

    @Transactional
    public void reindexAllData() {

        List<VehicleCompatibility> list = vehicleCompatibilityRepository.findAll();

        for (VehicleCompatibility vc : list) {

            try {
                VehicleCompatibilityDocument doc = mapToDocument(vc);
                vehicleCompatibilitySearchRepository.save(doc);
            } catch (Exception e) {
                System.out.println("Reindex failed for ID: " + vc.getId());
            }
        }

        System.out.println("Reindex completed");
    }


    // ─── MAPPER: VehicleCompatibility → ResponseDTO ───────────
    private VehicleCompatibilityResponseDTO mapToResponse(VehicleCompatibility vc) {
        return VehicleCompatibilityResponseDTO.builder()
                .id(vc.getId())
                .productId(vc.getProduct().getId())
                .productName(vc.getProduct().getName())
                .vehicleId(vc.getVehicle().getId())
                .vehicleBrand(vc.getVehicle().getBrand())
                .vehicleModel(vc.getVehicle().getModel())
                .vehicleYear(vc.getVehicle().getYear())
                .vehicleEngine(vc.getVehicle().getEngine())
                .vehiclePower(vc.getVehicle().getPower())
                .vehicleFuelType(vc.getVehicle().getFuelType().name())
                .vehicleTransmission(vc.getVehicle().getTransmission().name())
                .compatibilityDetails(vc.getCompatibilityDetails() != null
                        ? mapDetailsToResponse(vc.getCompatibilityDetails())
                        : null)
                .createdAt(vc.getCreatedAt())
                .build();
    }

    // ─── MAPPER: Product → ProductResponseDTO ────────────────
    private ProductResponseDTO mapProductToResponse(Product product) {
        List<String> compatibleVehicles = product.getVehicleCompatibilities().stream()
                .map(vc -> vc.getVehicle().getBrand() + " " +
                        vc.getVehicle().getModel() + " (" +
                        vc.getVehicle().getYear() + ")")
                .collect(Collectors.toList());

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .photoUrl(product.getPhotoUrl())
                .partNumber(product.getPartNumber())
                .company(product.getCompany())
                .actualPrice(product.getActualPrice())
                .discount(product.getDiscount())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .rating(product.getRating())
                .isActive(product.getIsActive())
                .subCategoryId(product.getSubCategory().getId())
                .subCategoryName(product.getSubCategory().getName())
                .categoryName(product.getSubCategory().getCategory().getName())
                .compatibleVehicles(compatibleVehicles)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    // ─── MAPPER: CompatibilityDetails → ResponseDTO ──────────
    private CompatibilityDetailsResponseDTO mapDetailsToResponse(CompatibilityDetails details) {
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
                .createdAt(details.getCreatedAt())
                .build();
    }

    // ─── MAPPER: VehicleCompatibility → ES Document ──────────
    private VehicleCompatibilityDocument mapToDocument(VehicleCompatibility vc) {
        Product product = vc.getProduct();
        Vehicle vehicle = vc.getVehicle();

        return VehicleCompatibilityDocument.builder()
                .id(vc.getId().toString())
                .productId(product.getId().toString())
                .productName(product.getName())
                .partNumber(product.getPartNumber())
                .company(product.getCompany())
                .categoryName(product.getSubCategory().getCategory().getName())
                .subCategoryName(product.getSubCategory().getName())
                .vehicleId(vehicle.getId().toString())
                .vehicleBrand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .engine(vehicle.getEngine())
                .power(vehicle.getPower())
                .fuelType(vehicle.getFuelType().name())
                .transmission(vehicle.getTransmission().name())
                .build();
    }
}


//package com.automobile.ecom.service;
//
//import com.automobile.ecom.document.VehicleCompatibilityDocument;
//import com.automobile.ecom.dto.ProductResponseDTO;
//import com.automobile.ecom.dto.VehicleCompatibilityRequestDTO;
//import com.automobile.ecom.dto.VehicleCompatibilityResponseDTO;
//import com.automobile.ecom.entity.Product;
//import com.automobile.ecom.entity.Vehicle;
//import com.automobile.ecom.entity.VehicleCompatibility;
//import com.automobile.ecom.exception.ResourceNotFoundException;
//import com.automobile.ecom.repository.ProductRepository;
//import com.automobile.ecom.repository.VehicleCompatibilityRepository;
//import com.automobile.ecom.repository.VehicleCompatibilitySearchRepository;
//import com.automobile.ecom.repository.VehicleRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class VehicleCompatibilityService {
//
//    private final VehicleCompatibilityRepository vehicleCompatibilityRepository;
//    private final ProductRepository productRepository;
//    private final VehicleRepository vehicleRepository;
//    private final VehicleCompatibilitySearchRepository vehicleCompatibilitySearchRepository;
//
//    // ─── CREATE ──────────────────────────────────────────────
//    @Transactional
//    public VehicleCompatibilityResponseDTO createCompatibility(VehicleCompatibilityRequestDTO dto) {
//        if (vehicleCompatibilityRepository.existsByProductIdAndVehicleId(
//                dto.getProductId(), dto.getVehicleId())) {
//            throw new IllegalArgumentException("Compatibility already exists for this product and vehicle");
//        }
//
//        Product product = productRepository.findByIdAndIsActiveTrue(dto.getProductId())
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));
//
//        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
//                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + dto.getVehicleId()));
//
//        VehicleCompatibility compatibility = VehicleCompatibility.builder()
//                .product(product)
//                .vehicle(vehicle)
//                .build();
//        VehicleCompatibility Compatibility = vehicleCompatibilityRepository.save(compatibility);
//        try {
//            VehicleCompatibilityDocument doc = mapToDocument(Compatibility);
//            vehicleCompatibilitySearchRepository.save(doc);
//        } catch (Exception e) {
//            System.out.println("ES sync failed: " + e.getMessage());
//        }
//
//        return mapToResponse(Compatibility);
////        return mapToResponse(vehicleCompatibilityRepository.save(compatibility));
//    }
//
//    // ─── GET ALL BY PRODUCT ───────────────────────────────────
//    @Transactional(readOnly = true)
//    public List<VehicleCompatibilityResponseDTO> getCompatibilitiesByProduct(Long productId) {
//        return vehicleCompatibilityRepository.findAllByProductId(productId)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    // ─── GET ALL PRODUCTS BY VEHICLE ──────────────────────────
//    @Transactional(readOnly = true)
//    public List<VehicleCompatibilityResponseDTO> getCompatibilitiesByVehicle(Long vehicleId) {
//        return vehicleCompatibilityRepository.findAllProductsByVehicleId(vehicleId)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    // ─── GET COMPATIBLE PRODUCTS BY BRAND/MODEL/YEAR ─────────
//    @Transactional(readOnly = true)
//    public List<VehicleCompatibilityResponseDTO> getCompatibleProducts(
//            String brand, String model, Integer year) {
//        return vehicleCompatibilityRepository.findCompatibleProducts(brand, model, year)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    // ─── GET BY ID ───────────────────────────────────────────
//    @Transactional(readOnly = true)
//    public VehicleCompatibilityResponseDTO getCompatibilityById(Long id) {
//        return mapToResponse(findCompatibilityById(id));
//    }
//
//    // ─── DELETE ──────────────────────────────────────────────
//    @Transactional
//    public void deleteCompatibility(Long id) {
//        VehicleCompatibility compatibility = findCompatibilityById(id);
////        vehicleCompatibilityRepository.delete(compatibility);
//        try {
//            vehicleCompatibilitySearchRepository.deleteById(id.toString());
//        } catch (Exception e) {
//            System.out.println("ES sync failed: " + e.getMessage());
//        }
//        vehicleCompatibilityRepository.delete(compatibility);
//
//    }
//
//    // ════════════════════════════════════════════════════════════
//    // ─── FILTER DROPDOWN METHODS ─────────────────────────────
//    // ════════════════════════════════════════════════════════════
//
//    // ─── STEP 1: all brands that have compatible products ────
//    @Transactional(readOnly = true)
//    public List<String> getFilterBrands() {
//        return vehicleCompatibilityRepository.findDistinctBrands();
//    }
//
//    // ─── STEP 2: fuel types by brand ─────────────────────────
//    @Transactional(readOnly = true)
//    public List<String> getFilterFuelTypes(String brand) {
//        return vehicleCompatibilityRepository.findDistinctFuelTypesByBrand(brand);
//    }
//
//    // ─── STEP 3: years by brand + fuelType ───────────────────
//    @Transactional(readOnly = true)
//    public List<Integer> getFilterYears(String brand, String fuelType) {
//        return vehicleCompatibilityRepository.findDistinctYearsByBrandAndFuelType(brand, fuelType);
//    }
//
//    // ─── STEP 4: models by brand + fuelType + year ───────────
//    @Transactional(readOnly = true)
//    public List<String> getFilterModels(String brand, String fuelType, Integer year) {
//        return vehicleCompatibilityRepository.findDistinctModelsByBrandFuelTypeAndYear(brand, fuelType, year);
//    }
//
//    // ─── STEP 5: engines by brand + fuelType + year + model ──
//    @Transactional(readOnly = true)
//    public List<String> getFilterEngines(String brand, String fuelType, Integer year, String model) {
//        return vehicleCompatibilityRepository.findProductsByFilter(brand, fuelType, year, model)
//                .stream()
//                .map(vc -> vc.getVehicle().getEngine())
//                .distinct()
//                .collect(Collectors.toList());
//    }
//
//    // ─── FINAL: products by brand + fuelType + year + model + engine ──
//    @Transactional(readOnly = true)
//    public List<ProductResponseDTO> getFilteredProducts(
//            String brand, String fuelType, Integer year, String model, String engine) {
//        return vehicleCompatibilityRepository.findProductsWithEngine(brand, fuelType, year, model, engine)
//                .stream()
//                .map(vc -> mapProductToResponse(vc.getProduct()))
//                .collect(Collectors.toList());
//    }
//
//    // ─── HELPER: find or throw ────────────────────────────────
//    private VehicleCompatibility findCompatibilityById(Long id) {
//        return vehicleCompatibilityRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Vehicle compatibility not found with id: " + id));
//    }
//
//    // ─── HELPER: VehicleCompatibility → response DTO ─────────
//    private VehicleCompatibilityResponseDTO mapToResponse(VehicleCompatibility vc) {
//        return VehicleCompatibilityResponseDTO.builder()
//                .id(vc.getId())
//                .vehicleId(vc.getVehicle().getId())
//                .vehicleBrand(vc.getVehicle().getBrand())
//                .model(vc.getVehicle().getModel())
//                .year(vc.getVehicle().getYear())
//                .engine(vc.getVehicle().getEngine())
//                .power(vc.getVehicle().getPower())
//                .fuelType(vc.getVehicle().getFuelType().name())
//                .createdAt(vc.getCreatedAt())
//                .build();
//    }
//
//    // ─── HELPER: Product → ProductResponseDTO ────────────────
//    private ProductResponseDTO mapProductToResponse(Product product) {
//        // Extract vehicle labels so the frontend can display the "Warning" data
//        List<String> compatibleVehicles = product.getVehicleCompatibilities().stream()
//                .map(v -> v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ")")
//                .collect(Collectors.toList());
//
//        return ProductResponseDTO.builder()
//                .id(product.getId())
//                .name(product.getName())
//                .description(product.getDescription())
//                .photoUrl(product.getPhotoUrl())
//                .partNumber(product.getPartNumber())
//                .company(product.getCompany())
//                .actualPrice(product.getActualPrice())
//                .discount(product.getDiscount())
//                .price(product.getPrice())
//                .stockQuantity(product.getStockQuantity())
//                .rating(product.getRating())
//                .isActive(product.getIsActive())
//                .subCategoryId(product.getSubCategory().getId())
//                .subCategoryName(product.getSubCategory().getName())
//                .categoryName(product.getSubCategory().getCategory().getName())
//                .compatibleVehicles(compatibleVehicles) // ESSENTIAL FOR ADMIN WARNINGS
//                .createdAt(product.getCreatedAt())
//                .updatedAt(product.getUpdatedAt())
//                .build();
//    }
//    private VehicleCompatibilityDocument mapToDocument(VehicleCompatibility vc) {
//
//        Product product = vc.getProduct();
//        Vehicle vehicle = vc.getVehicle();
//
//        return VehicleCompatibilityDocument.builder()
//
//                // ───────── ID ─────────
//                .id(vc.getId().toString())
//
//                // ───────── PRODUCT INFO ─────────
//                .productId(product.getId().toString())
//                .productName(product.getName())
//                .partNumber(product.getPartNumber())
//                .company(product.getCompany())
//                .categoryName(product.getSubCategory().getCategory().getName())
//                .subCategoryName(product.getSubCategory().getName())
//
//                // ───────── VEHICLE INFO ─────────
//                .vehicleId(vehicle.getId().toString())
//                .vehicleBrand(vehicle.getBrand())
//                .model(vehicle.getModel())
//                .year(vehicle.getYear())
//                .engine(vehicle.getEngine())
//                .power(vehicle.getPower())
//                .fuelType(vehicle.getFuelType().name())
//                .transmission(vehicle.getTransmission().name())
//
//                .build();
//    }
//
//}