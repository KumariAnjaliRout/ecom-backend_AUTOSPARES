package com.automobile.ecom.service;



import com.automobile.ecom.document.ProductDocument;
import com.automobile.ecom.dto.ProductRequestDTO;
import com.automobile.ecom.dto.ProductResponseDTO;
import com.automobile.ecom.entity.Product;
import com.automobile.ecom.entity.SubCategory;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.*;
import com.automobile.ecom.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final S3Service s3Service;
    private final ProductSearchRepository productSearchRepository;
    private final NameValidationService nameValidationService;
    private final AuditService auditService;
    private final OrderItemsRepository orderItemsRepository;

    private static final String S3_PREFIX = "https://autospares-images.s3.amazonaws.com/";

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {

        String cleanName = dto.getName() != null ? dto.getName().trim() : null;
        String cleanPartNumber = dto.getPartNumber() != null ? dto.getPartNumber().trim() : null;

        if (cleanName == null || cleanName.isEmpty()) {
            throw new BadRequestException("Product name is required");
        }

        if (cleanPartNumber == null || cleanPartNumber.isEmpty()) {
            throw new BadRequestException("Part number is required");
        }

        if (dto.getActualPrice() != null &&
                dto.getActualPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Price cannot be negative");
        }

        if (dto.getDiscount() != null &&
                (dto.getDiscount() < 0 || dto.getDiscount() > 100)) {
            throw new BadRequestException("Discount must be between 0 and 100");
        }
        nameValidationService.validateUniqueName(cleanName);

        if (productRepository.existsByPartNumber(cleanPartNumber)) {
            throw new BadRequestException("Product with part number already exists");
        }

        SubCategory subCategory = subCategoryRepository.findById(dto.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));

        if (!subCategory.getIsActive() || !subCategory.getCategory().getIsActive()) {
            throw new BadRequestException("Cannot create product in inactive hierarchy");
        }

        //  Removed S3_PREFIX validation — controller already handles upload
        Product product = Product.builder()
                .name(cleanName)
                .description(dto.getDescription())
                .photoUrl(dto.getImageUrl())
                .partNumber(cleanPartNumber)
                .company(dto.getCompany())
                .actualPrice(dto.getActualPrice())
                .discount(dto.getDiscount())
                .stockQuantity(dto.getStockQuantity())
                .subCategory(subCategory)
                .isActive(true)
                .totalReviews(0).build();
        Product saved = productRepository.save(product);
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "PRODUCT_CREATED",
                "PRODUCT",
                String.valueOf(product.getId()),
                "Product created: " + saved.getName()
        );
        //  ES SYNC
        try {
            ProductDocument doc = mapToDocument(saved);
            productSearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductByIdAdmin(Long id) {
        return mapToResponse(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    // ─── GET ALL ACTIVE ──────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllActiveProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponse);
    }

    // ─── GET ALL ADMIN ───────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProductsForAdmin(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // ─── GET BY SUBCATEGORY ──────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsBySubCategory(Long subCategoryId, Pageable pageable) {

        SubCategory subCategory = subCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));

        if (!subCategory.getIsActive() || !subCategory.getCategory().getIsActive()) {
            throw new ResourceNotFoundException("Category unavailable");
        }

        return productRepository
                .findAllBySubCategoryIdAndIsActiveTrue(subCategoryId, pageable)
                .map(this::mapToResponse);
    }

    // ─── GET BY COMPANY ──────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByCompany(String company, Pageable pageable) {
        return productRepository.findAllByCompany(company, pageable)
                .map(this::mapToResponse);
    }

    // ─── GET BY ID USER ──────────────────────────────────────
    @Transactional(readOnly = true)
    public ProductResponseDTO getActiveProductById(Long id) {

        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSubCategory().getIsActive() ||
                !product.getSubCategory().getCategory().getIsActive()) {
            throw new ResourceNotFoundException("Product unavailable");
        }

        return mapToResponse(product);
    }

    // ─── GET BY PART NUMBER ──────────────────────────────────
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductByPartNumber(String partNumber) {

        if (partNumber == null || partNumber.trim().isEmpty()) {
            throw new BadRequestException("Part number is required");
        }

        String cleanPartNumber = partNumber.trim();

        Product product = productRepository
                .findByPartNumberIgnoreCaseAndIsActiveTrue(cleanPartNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSubCategory().getIsActive() ||
                !product.getSubCategory().getCategory().getIsActive()) {
            throw new ResourceNotFoundException("Product unavailable");
        }

        return mapToResponse(product);
    }
    // ─── UPDATE ──────────────────────────────────────────────
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        String cleanName = dto.getName() != null ? dto.getName().trim() : null;
        String cleanPartNumber = dto.getPartNumber() != null ? dto.getPartNumber().trim() : null;

        if (cleanName == null || cleanName.isEmpty()) {
            throw new BadRequestException("Product name is required");
        }

        if (cleanPartNumber == null || cleanPartNumber.isEmpty()) {
            throw new BadRequestException("Part number is required");
        }

        if (dto.getActualPrice() == null ||
                dto.getActualPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Invalid price");
        }

        if (dto.getDiscount() != null &&
                (dto.getDiscount() < 0 || dto.getDiscount() > 100)) {
            throw new BadRequestException("Invalid discount");
        }

        if (dto.getStockQuantity() == null || dto.getStockQuantity() < 0) {
            throw new BadRequestException("Invalid stock");
        }
        nameValidationService.validateUniqueNameForProductUpdate(cleanName, id);
        if (!product.getPartNumber().equalsIgnoreCase(cleanPartNumber)
                && productRepository.existsByPartNumber(cleanPartNumber)) {
            throw new BadRequestException("Product with part number already exists");
        }

        SubCategory subCategory = subCategoryRepository.findById(dto.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));

        if (!subCategory.getIsActive() || !subCategory.getCategory().getIsActive()) {
            throw new BadRequestException("Cannot assign product to inactive hierarchy");
        }

        // ✅ Removed S3_PREFIX validation — controller already handles upload

        // ─── DELETE OLD IMAGE IF CHANGED ────────────────────────
        if (product.getPhotoUrl() != null &&
                dto.getImageUrl() != null &&
                !dto.getImageUrl().equals(product.getPhotoUrl())) {
            s3Service.deleteFile(product.getPhotoUrl());
        }

        // ─── UPDATE FIELDS ──────────────────────────────────────
        product.setName(cleanName);
        product.setDescription(dto.getDescription());
        product.setPartNumber(cleanPartNumber);
        product.setCompany(dto.getCompany().trim());
        product.setActualPrice(dto.getActualPrice());
        product.setDiscount(dto.getDiscount());
        product.setStockQuantity(dto.getStockQuantity());
        product.setSubCategory(subCategory);
        product.setPhotoUrl(dto.getImageUrl());
        Product saved = productRepository.save(product);
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "PRODUCT_UPDATED",
                "PRODUCT",
                String.valueOf(product.getId()),
                "Updated product: " + saved.getName()
        );
        // ✅ ES SYNC
        try {
            ProductDocument doc = mapToDocument(saved);
            productSearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(productRepository.save(product));
    }

    // ─── TOGGLE STATUS ───────────────────────────────────────
    @Transactional
    public ProductResponseDTO toggleProductStatus(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        boolean newStatus = !product.getIsActive();

        if (newStatus) {
            if (!product.getSubCategory().getIsActive() ||
                    !product.getSubCategory().getCategory().getIsActive()) {
                throw new BadRequestException("Cannot activate product under inactive category");
            }
        }

        product.setIsActive(newStatus);
        Product saved = productRepository.save(product);
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                saved.getIsActive() ? "PRODUCT_ACTIVATED" : "PRODUCT_DEACTIVATED",
                "PRODUCT",
                String.valueOf(product.getId()),
                "Product status changed"
        );
        // ✅ ES SYNC
        try {
            ProductDocument doc = mapToDocument(saved);
            productSearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(productRepository.save(product));
    }

    // ─── DECREASE STOCK ──────────────────────────────────────
    @Transactional
    public void decreaseStock(Long id, int quantity) {

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        int updated = productRepository.decreaseStock(id, quantity);
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "STOCK_UPDATED",
                "PRODUCT",
                String.valueOf(id),
                "Stock decreased by " + quantity
        );

        if (updated == 0) {
            throw new BadRequestException("Insufficient stock or product not found");
        }
    }

    // ─── DELETE ──────────────────────────────────────────────
    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));


        if (product.getVehicleCompatibilities() != null && !product.getVehicleCompatibilities().isEmpty()) {
            throw new BadRequestException("Cannot delete product: It is currently linked to vehicles. Remove compatibility first or deactivate it instead.");
        }


        if (product.getPhotoUrl() != null) {
            s3Service.deleteFile(product.getPhotoUrl());
        }
        try {
            productSearchRepository.deleteById(id.toString());

        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "PRODUCT_DELETED",
                "PRODUCT",
                String.valueOf(product.getId()),
                "Deleted product: " + product.getName()
        );
        productRepository.delete(product);

//        productRepository.delete(product);
    }

    public List<ProductResponseDTO> getUnsoldProducts() {
        return orderItemsRepository.findUnsoldProducts()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public Map<String, Long> getProductStatusStats() {
        return Map.of(
                "active", productRepository.countByIsActiveTrue(),
                "inactive", productRepository.countByIsActiveFalse()
        );
    }

    public Map<String, Long> getCategoryStats() {
        return Map.of(
                "active", categoryRepository.countByIsActiveTrue(),
                "inactive", categoryRepository.countByIsActiveFalse()
        );
    }

    public Map<String, Long> getSubCategoryStats() {
        return Map.of(
                "active", subCategoryRepository.countByIsActiveTrue(),
                "inactive", subCategoryRepository.countByIsActiveFalse()
        );
    }

    public List<ProductResponseDTO> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public List<ProductResponseDTO> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public List<ProductResponseDTO> getTopRatedProducts(int limit) {
        return productRepository.findTopRatedProducts(Pageable.ofSize(limit))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public List<Map<String, Object>> getTopCategories(int limit) {
        return productRepository.findTopSellingCategories(Pageable.ofSize(limit))
                .stream()
                .map(obj -> Map.of(
                        "category", obj[0],
                        "totalSold", obj[1]
                ))
                .toList();
    }

    private CustomUserPrincipal getCurrentUser() {
        return (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // ─── MAPPER ──────────────────────────────────────────────
    private ProductResponseDTO mapToResponse(Product product) {

        List<String> compatibleVehicles = product.getVehicleCompatibilities() != null
                ? product.getVehicleCompatibilities().stream()
                .map(v -> v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ")")
                .collect(Collectors.toList())
                : List.of();

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
    private ProductDocument mapToDocument(Product product) {
        List<String> compatibleVehicles = product.getVehicleCompatibilities().stream()
                .map(v -> v.getBrand() + " " + v.getModel() + " (" + v.getYear() + ")")
                .collect(Collectors.toList());


        return ProductDocument.builder()
                .id(product.getId().toString())

                // 🔹 Basic Info
                .name(product.getName())
                .description(product.getDescription())
                .photoUrl(product.getPhotoUrl())
                .partNumber(product.getPartNumber())
                .company(product.getCompany())

                // 🔹 Pricing
                .actualPrice(product.getActualPrice())
                .discount(product.getDiscount())
                .price(product.getPrice())

                // 🔹 Inventory
                .stockQuantity(product.getStockQuantity())
                .rating(product.getRating())
                .isActive(product.getIsActive())

                // 🔹 Category Hierarchy
                .categoryId(product.getSubCategory().getCategory().getId().toString())
                .categoryName(product.getSubCategory().getCategory().getName())

                .subCategoryId(product.getSubCategory().getId().toString())
                .subCategoryName(product.getSubCategory().getName())
                .compatibleVehicles(compatibleVehicles)

                // 🔹 Metadata
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())

                .build();
    }
}