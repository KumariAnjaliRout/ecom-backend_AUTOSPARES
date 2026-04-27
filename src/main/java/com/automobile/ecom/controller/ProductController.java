package com.automobile.ecom.controller;


import com.automobile.ecom.dto.ProductRequestDTO;
import com.automobile.ecom.dto.ProductResponseDTO;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.service.ProductService;
import com.automobile.ecom.service.S3Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final S3Service s3Service;

    private static final String PRODUCT_FOLDER = "products";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ─── CREATE (admin) ───────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Product image is required");
        }

        ProductRequestDTO dto = parseDto(dataJson);
        validateFile(file);
        dto.setImageUrl(s3Service.uploadFile(file, PRODUCT_FOLDER));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(dto));
    }

    // ─── UPDATE (admin) ───────────────────────────────────────
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "existingPhotoUrl", required = false) String existingPhotoUrl) {

        ProductRequestDTO dto = parseDto(dataJson);

        if (file != null && !file.isEmpty()) {
            validateFile(file);
            dto.setImageUrl(s3Service.uploadFile(file, PRODUCT_FOLDER));
        } else {
            dto.setImageUrl(existingPhotoUrl);
        }

        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    // ─── GET ALL ACTIVE (public) ──────────────────────────────
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllActiveProducts(
            @PageableDefault(page=0,size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.getAllActiveProducts(pageable));
    }

    // ─── GET ALL including inactive (admin) ──────────────────
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProductsForAdmin(
            @PageableDefault(page=0,size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProductsForAdmin(pageable));
    }

    // ─── GET BY ID (public) ───────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getActiveProductById(id));
    }

    // ─── GET BY ID (admin) ────────────────────────────────────
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> getProductByIdAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductByIdAdmin(id));
    }

    // ─── SEARCH BY PART NUMBER ────────────────────────────────
    @GetMapping("/part-number/{partNumber}")
    public ResponseEntity<ProductResponseDTO> getProductByPartNumber(
            @PathVariable String partNumber) {
        return ResponseEntity.ok(productService.getProductByPartNumber(partNumber));
    }

    // ─── SEARCH BY SUBCATEGORY ────────────────────────────────
    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsBySubCategory(
            @PathVariable Long subCategoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsBySubCategory(subCategoryId, pageable));
    }

    // ─── SEARCH BY COMPANY ────────────────────────────────────
    @GetMapping("/company/{company}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCompany(
            @PathVariable String company,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCompany(company, pageable));
    }

    // ─── TOGGLE STATUS ────────────────────────────────────────
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponseDTO> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleProductStatus(id));
    }

    // ─── DECREASE STOCK ───────────────────────────────────────
    @PatchMapping("/{id}/decrease-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> decreaseStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        productService.decreaseStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    // ─── DELETE ───────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ─── FILE VALIDATION HELPER ───────────────────────────────
    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/webp"))) {
            throw new BadRequestException("Only JPEG, PNG, WEBP images are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size must be less than 5MB");
        }
    }

    // ─── JSON PARSER HELPER ───────────────────────────────────
    private ProductRequestDTO parseDto(String json) {
        try {
            return objectMapper.readValue(json, ProductRequestDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Invalid JSON data: " + e.getMessage());
        }
    }

    @GetMapping("/low-stock")
    public List<ProductResponseDTO> lowStock(
            @RequestParam(defaultValue = "10") int threshold) {

        return productService.getLowStockProducts(threshold);
    }
    @GetMapping("/out-of-stock")
    public List<ProductResponseDTO> outOfStock() {
        return productService.getOutOfStockProducts();
    }
    @GetMapping("/top-rated")
    public List<ProductResponseDTO> topRated(
            @RequestParam(defaultValue = "5") int limit) {

        return productService.getTopRatedProducts(limit);
    }

    @GetMapping("/top-categories")
    public List<Map<String, Object>> topCategories(
            @RequestParam(defaultValue = "5") int limit) {

        return productService.getTopCategories(limit);
    }

    @GetMapping("/admin/products/unsold")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponseDTO>> getUnsoldProducts() {
        return ResponseEntity.ok(productService.getUnsoldProducts());
    }

}