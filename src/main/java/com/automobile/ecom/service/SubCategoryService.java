package com.automobile.ecom.service;

import com.automobile.ecom.document.SubCategoryDocument;
import com.automobile.ecom.dto.SubCategoryRequestDTO;
import com.automobile.ecom.dto.SubCategoryResponseDTO;
import com.automobile.ecom.entity.Categories;
import com.automobile.ecom.entity.SubCategory;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.CategoryRepository;
import com.automobile.ecom.repository.SubCategoryRepository;
import com.automobile.ecom.repository.SubCategorySearchRepository;
import com.automobile.ecom.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final AuditService auditService;
    private final SubCategorySearchRepository subCategorySearchRepository;
    private final NameValidationService nameValidationService;

    // ─── CREATE ──────────────────────────────────────────────
    @Transactional
    public SubCategoryResponseDTO createSubCategory(SubCategoryRequestDTO dto) {

        // 1. Blank check FIRST (before any DB call)
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BadRequestException("SubCategory name cannot be empty");
        }
        String normalizedName = dto.getName().trim();

        // 2. Parent category existence check
        Categories category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        // 3. Parent active check
        if (!category.getIsActive()) {
            throw new BadRequestException("Cannot create subcategory under an inactive category");
        }
        nameValidationService.validateUniqueName(normalizedName);
        // 4. Duplicate check (scoped to same category)
        if (subCategoryRepository.existsByNameIgnoreCaseAndCategoryId(normalizedName, dto.getCategoryId())) {
            throw new BadRequestException("SubCategory '" + normalizedName + "' already exists in this category");
        }

        // 5. Image URL integrity check
        validatePhotoUrl(dto.getPhotoUrl());

        SubCategory subCategory = SubCategory.builder()
                .name(normalizedName)
                .photoUrl(dto.getPhotoUrl())
                .category(category)
                .isActive(true)
                .build();

        SubCategory saved = subCategoryRepository.save(subCategory);
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "SUBCATEGORY_CREATED",
                "SUBCATEGORY",
                String.valueOf(subCategory.getId()),
                "Created subcategory: " + saved.getName()
        );
//        log.info("SubCategory created: id={}, name={}, categoryId={}", saved.getId(), saved.getName(), dto.getCategoryId());
        try {
            SubCategoryDocument doc = mapToDocument(saved);
            subCategorySearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
        return mapToResponse(saved);
    }

    // ─── GET ALL ACTIVE BY CATEGORY (public shop page) ───────
    @Transactional(readOnly = true)
    public List<SubCategoryResponseDTO> getActiveSubCategoriesByCategory(Long categoryId) {
        // 1. Verify parent category exists and is active
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (!category.getIsActive()) {
            // Parent is inactive — no subcategories should be visible publicly
            return Collections.emptyList();
        }

        // 2. Fetch active subcategories (sorted by name)
        return subCategoryRepository.findAllByCategoryIdAndIsActiveTrueOrderByNameAsc(categoryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET ALL BY CATEGORY (admin dashboard) ────────────────
    @Transactional(readOnly = true)
    public List<SubCategoryResponseDTO> getAllSubCategoriesByCategory(Long categoryId) {
        // Verify parent category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }

        // Fetch all subcategories (sorted by name)
        return subCategoryRepository.findAllByCategoryIdOrderByNameAsc(categoryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public SubCategoryResponseDTO getSubCategoryById(Long id, boolean isAdminView) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with id: " + id));

        // Fix: hide inactive subcategories from public view
        if (!isAdminView && !subCategory.getIsActive()) {
            throw new ResourceNotFoundException("SubCategory not found with id: " + id);
        }

        return mapToResponse(subCategory);
    }

    // ─── UPDATE ──────────────────────────────────────────────
    @Transactional
    public SubCategoryResponseDTO updateSubCategory(Long id, SubCategoryRequestDTO dto) {

        // 1. Blank check FIRST
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BadRequestException("SubCategory name cannot be empty");
        }
        String normalizedName = dto.getName().trim();

        // 2. Fetch subcategory
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with id: " + id));

        // 3. Duplicate name check (scoped to same category, exclude self)
        if (subCategoryRepository.existsByNameIgnoreCaseAndCategoryIdAndIdNot(normalizedName, dto.getCategoryId(), id)) {
            throw new BadRequestException("SubCategory '" + normalizedName + "' already exists in this category");
        }
        Long targetCategoryId = (dto.getCategoryId() != null)
                ? dto.getCategoryId()
                : subCategory.getCategory().getId();
        nameValidationService.validateUniqueName(normalizedName);

        // 4. Image URL integrity check
        validatePhotoUrl(dto.getPhotoUrl());

        // 5. S3 cleanup — delete old image only if URL has changed
        String oldUrl = subCategory.getPhotoUrl();
        String newUrl = dto.getPhotoUrl();

        if (oldUrl != null && !oldUrl.equals(newUrl)) {
            try {
                s3Service.deleteFile(oldUrl);
                log.info("Deleted old S3 image: {}", oldUrl);
            } catch (Exception e) {
                // Log but don't fail the update — S3 cleanup is best-effort
                log.warn("Failed to delete old S3 image: {}. Error: {}", oldUrl, e.getMessage());
            }
        }

        subCategory.setName(normalizedName);
        subCategory.setPhotoUrl(newUrl);

        // 6. Category move logic
        if (!subCategory.getCategory().getId().equals(dto.getCategoryId())) {
            Categories newCategory = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target category not found with id: " + dto.getCategoryId()));

            // Guard: cannot move active subcategory to inactive category
            if (subCategory.getIsActive() && !newCategory.getIsActive()) {
                throw new BadRequestException("Cannot move an active subcategory to an inactive category");
            }

            subCategory.setCategory(newCategory);
            log.info("SubCategory id={} moved to categoryId={}", id, dto.getCategoryId());
        }

        SubCategory saved = subCategoryRepository.save(subCategory);
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "SUBCATEGORY_UPDATED",
                "SUBCATEGORY",
                String.valueOf(subCategory.getId()),
                "Updated subcategory: " + saved.getName()
        );
        log.info("SubCategory updated: id={}, name={}", saved.getId(), saved.getName());
        try {
            SubCategoryDocument doc = mapToDocument(saved);
            subCategorySearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }
        return mapToResponse(saved);
    }

    // ─── TOGGLE ACTIVE ────────────────────────────────────────
    @Transactional
    public SubCategoryResponseDTO toggleSubCategoryStatus(Long id) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with id: " + id));

        boolean newStatus = !subCategory.getIsActive();

        // Hierarchy guard: cannot activate if parent category is inactive
        if (newStatus && !subCategory.getCategory().getIsActive()) {
            throw new BadRequestException("Cannot activate subcategory while the parent category is inactive");
        }

        subCategory.setIsActive(newStatus);

        // Cascade deactivation to products
        if (!newStatus && subCategory.getProducts() != null) {
            subCategory.getProducts().forEach(p -> p.setIsActive(false));
        }

        SubCategory saved = subCategoryRepository.save(subCategory);
        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                saved.getIsActive() ? "SUBCATEGORY_ACTIVATED" : "SUBCATEGORY_DEACTIVATED",
                "SUBCATEGORY",
                String.valueOf(subCategory.getId()),
                "Subcategory status changed"
        );
        log.info("SubCategory id={} status toggled to: {}", id, newStatus);
        try {
            SubCategoryDocument doc = mapToDocument(saved);
            subCategorySearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
    }
    @Transactional
    public void deleteSubCategory(Long id) {

        // JOIN FETCH — products + category ek query mein (N+1 fix)
        SubCategory subCategory = subCategoryRepository.findByIdWithProductsAndCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("SubCategory not found with id: " + id));

        // Guard: products exist hain to delete nahi kar sakte
        if (subCategory.getProducts() != null && !subCategory.getProducts().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete SubCategory '" + subCategory.getName() +
                            "' because it has " + subCategory.getProducts().size() + " product(s) associated with it");
        }

        // S3 image cleanup
        if (subCategory.getPhotoUrl() != null) {
            try {
                s3Service.deleteFile(subCategory.getPhotoUrl());
                log.info("Deleted S3 image for SubCategory id={}", id);
            } catch (Exception e) {
                log.warn("Failed to delete S3 image for SubCategory id={}. Error: {}", id, e.getMessage());
            }
        }

        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "SUBCATEGORY_DELETED",
                "SUBCATEGORY",
                String.valueOf(subCategory.getId()),
                "Deleted subcategory: " + subCategory.getName()
        );
        subCategoryRepository.delete(subCategory);
        log.info("SubCategory deleted: id={}, name={}", id, subCategory.getName());
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────

    // Fix: extracted shared S3 URL validation into one place
    private void validatePhotoUrl(String photoUrl) {
        if (photoUrl != null && !photoUrl.isBlank()
                && !photoUrl.startsWith("https://autospares-images.s3")) {
            throw new BadRequestException("Invalid image source. Only S3 hosted images are allowed.");
        }
    }

    private CustomUserPrincipal getCurrentUser() {
        return (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // Fix: mapToResponse now populates productCount and activeProductCount
    private SubCategoryResponseDTO mapToResponse(SubCategory subCategory) {
        List<com.automobile.ecom.entity.Product> products = subCategory.getProducts();

        int totalProducts = (products != null) ? products.size() : 0;
        int activeProducts = (products != null)
                ? (int) products.stream()
                .filter(com.automobile.ecom.entity.Product::getIsActive)
                .count()
                : 0;

        return SubCategoryResponseDTO.builder()
                .id(subCategory.getId())
                .name(subCategory.getName())
                .photoUrl(subCategory.getPhotoUrl())
                .isActive(subCategory.getIsActive())
                .categoryId(subCategory.getCategory().getId())
                .categoryName(subCategory.getCategory().getName())
                .productCount(totalProducts)
                .activeProductCount(activeProducts)
                .createdAt(subCategory.getCreatedAt())
                .updatedAt(subCategory.getUpdatedAt())
                .build();
    }
    private SubCategoryDocument mapToDocument(SubCategory subCategory) {
        return SubCategoryDocument.builder()
                .id(subCategory.getId().toString())
                .name(subCategory.getName())
                .photoUrl(subCategory.getPhotoUrl())
                .isActive(subCategory.getIsActive())
                .categoryId(subCategory.getCategory().getId().toString())
                .categoryName(subCategory.getCategory().getName())
                .build();
    }
}