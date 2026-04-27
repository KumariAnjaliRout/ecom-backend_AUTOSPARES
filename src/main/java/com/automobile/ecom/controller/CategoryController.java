package com.automobile.ecom.controller;


import com.automobile.ecom.dto.CategoryRequestDTO;
import com.automobile.ecom.dto.CategoryResponseDTO;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.service.CategoryService;
import com.automobile.ecom.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final S3Service s3Service;

    // ─── CREATE ──────────────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @RequestParam("name") String name,   // ← changed
            @RequestPart(value = "file", required = false) MultipartFile file) {

        // 1. Validate name first — before touching S3
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }

        // 2. Upload image only after name is validated
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            validateImageFile(file);
            imageUrl = s3Service.uploadFile(file, "categories");
        }

        CategoryRequestDTO dto = CategoryRequestDTO.builder()
                .name(name.trim())
                .photoUrl(imageUrl)
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(dto));
    }

    // ─── PUBLIC: active categories for shop page ──────────────
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    // ─── ADMIN: all categories including inactive ─────────────
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // ─── PUBLIC: single category by ID ───────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // ─── ADMIN: single category by ID (full details) ──────────
    // Fix: added a dedicated admin endpoint that passes isAdminView=true
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> getCategoryByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // ─── ADMIN: update category ───────────────────────────────
    // Fix: validate name before uploading new image to S3
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,                                    // ← changed
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "existingPhotoUrl", required = false) String existingPhotoUrl) {

        // 1. Validate name first — before touching S3
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }

        // 2. Upload new image only if a new file was provided
        String imageUrl;
        if (file != null && !file.isEmpty()) {
            validateImageFile(file);
            imageUrl = s3Service.uploadFile(file, "categories");
        } else {
            // No new file — keep existing image URL
            imageUrl = existingPhotoUrl;
        }

        CategoryRequestDTO dto = CategoryRequestDTO.builder()
                .name(name.trim())
                .photoUrl(imageUrl)
                .build();

        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // ─── ADMIN: toggle active/inactive ────────────────────────
    // Deactivating cascades to subcategories and products.
    // Reactivating does NOT auto-reactivate children.
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.toggleCategoryStatus(id));
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────

    // Fix: file type validation before S3 upload
    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed (jpg, png, webp, etc.)");
        }

        // Max file size: 5MB
        long maxSizeBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException("Image file size must not exceed 5MB");
        }
    }
}