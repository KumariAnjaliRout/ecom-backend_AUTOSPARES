package com.automobile.ecom.service;
import com.automobile.ecom.document.CategoryDocument;
import com.automobile.ecom.dto.CategoryRequestDTO;
import com.automobile.ecom.dto.CategoryResponseDTO;
import com.automobile.ecom.entity.Categories;
import com.automobile.ecom.entity.SubCategory;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.CategoryRepository;
import com.automobile.ecom.repository.CategorySearchRepository;
import com.automobile.ecom.repository.SubCategoryRepository;
import com.automobile.ecom.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final CategorySearchRepository categorySearchRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final AuditService auditService;

    // ─── CREATE ──────────────────────────────────────────────
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }

        Categories category = Categories.builder()
                .name(dto.getName())
                .photoUrl(dto.getPhotoUrl())
                .isActive(true)
                .build();

//        return mapToResponse(categoryRepository.save(category));
        Categories saved = categoryRepository.save(category);

        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "CATEGORY_CREATED",
                "CATEGORY",
                String.valueOf(category.getId()),
                "Created category: " + saved.getName()
        );

        //  Save in Elasticsearch
        try {
            CategoryDocument doc = mapToDocument(saved);
            categorySearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
    }

    // ─── GET ALL ACTIVE (for users) ───────────────────────────
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllActiveCategories() {
        return categoryRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET ALL (for admin) ──────────────────────────────────
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    // ─── UPDATE ──────────────────────────────────────────────
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }

        // Only delete old S3 image if the URL has actually changed
        String oldPhotoUrl = category.getPhotoUrl();
        String newPhotoUrl = dto.getPhotoUrl();
        if (oldPhotoUrl != null && newPhotoUrl != null && !newPhotoUrl.equals(oldPhotoUrl)) {
            s3Service.deleteFile(oldPhotoUrl);
        }

        category.setName(dto.getName());
        category.setPhotoUrl(newPhotoUrl);

//        return mapToResponse(categoryRepository.save(category));

        Categories updated = categoryRepository.save(category);

        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "CATEGORY_UPDATED",
                "CATEGORY",
                String.valueOf(category.getId()),
                "Updated category: " + updated.getName()
        );

        //  Sync to ES
        try {
            CategoryDocument doc = mapToDocument(updated);
            categorySearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES ERROR: " + e.getMessage());
        }

        return mapToResponse(updated);
    }
    @Transactional
    public void deleteCategory(Long id) {

        Categories category = categoryRepository.findByIdWithSubCategoriesOnly(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<SubCategory> subs = category.getSubCategories();

        if (subs != null && !subs.isEmpty()) {
            // Load products separately — no MultipleBagFetchException
            List<SubCategory> subsWithProducts =
                    subCategoryRepository.findSubCategoriesWithProductsByCategoryId(id);

            boolean hasProducts = subsWithProducts.stream()
                    .anyMatch(sub -> sub.getProducts() != null && !sub.getProducts().isEmpty());

            if (hasProducts) {
                throw new BadRequestException(
                        "Category cannot be deleted because it contains products. " +
                                "Please remove all products first."
                );
            }
        }

        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                "CATEGORY_DELETED",
                "CATEGORY",
                String.valueOf(category.getId()),
                "Deleted category: " + category.getName()
        );

        categoryRepository.delete(category);
        log.info("Category hard deleted: id={}, name={}", id, category.getName());
    }

    // ─── TOGGLE ACTIVE ────────────────────────────────────────
    @Transactional
    public CategoryResponseDTO toggleCategoryStatus(Long id) {
        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean newStatus = !category.getIsActive();
        category.setIsActive(newStatus);

        // Only cascade DOWNWARD on deactivation.
        // On reactivation, children stay as-is — admin reactivates them manually.
        if (!newStatus && category.getSubCategories() != null) {
            category.getSubCategories().forEach(sub -> {
                sub.setIsActive(false);
                if (sub.getProducts() != null) {
                    sub.getProducts().forEach(product -> product.setIsActive(false));
                }
            });
        }
        Categories saved = categoryRepository.save(category);

        CustomUserPrincipal user = getCurrentUser();

        auditService.logAction(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                saved.getIsActive() ? "CATEGORY_ACTIVATED" : "CATEGORY_DEACTIVATED",
                "CATEGORY",
                String.valueOf(category.getId()),
                "Category status changed"
        );

        // ✅ Save in Elasticsearch
        try {
            CategoryDocument doc = mapToDocument(saved);
            categorySearchRepository.save(doc);
        } catch (Exception e) {
            System.out.println("ES sync failed: " + e.getMessage());
        }

        return mapToResponse(saved);
//        return mapToResponse(categoryRepository.save(category));
    }

    private CustomUserPrincipal getCurrentUser() {
        return (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private CategoryResponseDTO mapToResponse(Categories category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .photoUrl(category.getPhotoUrl())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
    private CategoryDocument mapToDocument(Categories category) {
        return CategoryDocument.builder()
                .id(category.getId().toString())
                .name(category.getName())
                .photoUrl(category.getPhotoUrl())
                .isActive(category.getIsActive())
                .build();
    }
}
