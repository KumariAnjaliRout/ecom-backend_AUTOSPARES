package com.automobile.ecom.controller;

import com.automobile.ecom.dto.SubCategoryRequestDTO;
import com.automobile.ecom.dto.SubCategoryResponseDTO;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.service.S3Service;
import com.automobile.ecom.service.SubCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubCategoryController {

    private final SubCategoryService subCategoryService;
    private final S3Service s3Service;

    // ─── CREATE (admin) ───────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryResponseDTO> createSubCategory(
            @RequestPart("file") MultipartFile file,
            @RequestPart("name") String name,
            @RequestPart("categoryId") String categoryId) {

        String imageUrl = s3Service.uploadFile(file, "subcategories");

        SubCategoryRequestDTO dto = new SubCategoryRequestDTO();
        dto.setName(name);
        dto.setPhotoUrl(imageUrl);

        try {
            dto.setCategoryId(Long.parseLong(categoryId));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid categoryId: " + categoryId);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subCategoryService.createSubCategory(dto));
    }

    // ─── GET ALL ACTIVE BY CATEGORY (public) ──────────────────
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubCategoryResponseDTO>> getActiveSubCategories(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(
                subCategoryService.getActiveSubCategoriesByCategory(categoryId));
    }

    // ─── GET ALL BY CATEGORY (admin) ──────────────────────────
    @GetMapping("/admin/category/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubCategoryResponseDTO>> getAllSubCategories(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(
                subCategoryService.getAllSubCategoriesByCategory(categoryId));
    }

    // ─── GET BY ID (public — hides inactive) ──────────────────
    @GetMapping("/{id}")
    public ResponseEntity<SubCategoryResponseDTO> getSubCategoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                subCategoryService.getSubCategoryById(id, false));
    }

    // ─── GET BY ID (admin — shows inactive) ───────────────────
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryResponseDTO> getSubCategoryByIdAdmin(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                subCategoryService.getSubCategoryById(id, true));
    }

    // ─── UPDATE (admin) ───────────────────────────────────────
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryResponseDTO> updateSubCategory(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("name") String name,
            @RequestPart("categoryId") String categoryId,
            @RequestPart(value = "existingPhotoUrl", required = false) String existingPhotoUrl) {

        String imageUrl = (file != null && !file.isEmpty())
                ? s3Service.uploadFile(file, "subcategories")
                : existingPhotoUrl;

        SubCategoryRequestDTO dto = new SubCategoryRequestDTO();
        dto.setName(name);
        dto.setPhotoUrl(imageUrl);

        try {
            dto.setCategoryId(Long.parseLong(categoryId));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid categoryId: " + categoryId);
        }

        return ResponseEntity.ok(subCategoryService.updateSubCategory(id, dto));
    }

    // ─── TOGGLE STATUS (admin) ────────────────────────────────
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubCategoryResponseDTO> toggleStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                subCategoryService.toggleSubCategoryStatus(id));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubCategory(
            @PathVariable Long id) {
        subCategoryService.deleteSubCategory(id);
        return ResponseEntity.noContent().build();
    }
}