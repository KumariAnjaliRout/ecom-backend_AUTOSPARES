package com.automobile.ecom.service;


import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.repository.CategoryRepository;
import com.automobile.ecom.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.automobile.ecom.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class NameValidationService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;


    // ─── CREATE ke liye ───────────────────────────────────────
    public void validateUniqueName(String name) {

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Name '" + name + "' already exists as a Category");
        }

        if (subCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Name '" + name + "' already exists as a SubCategory");
        }

        if (productRepository.existsByNameIgnoreCase(name)) {  // ← add karo
            throw new BadRequestException("Name '" + name + "' already exists as a Product");
        }
    }

    // ─── SubCategory UPDATE ke liye ───────────────────────────
    public void validateUniqueNameForSubCategoryUpdate(String name, Long excludeId) {

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Name '" + name + "' already exists as a Category");
        }

        if (subCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)) {
            throw new BadRequestException("Name '" + name + "' already exists as a SubCategory");
        }

        if (productRepository.existsByNameIgnoreCase(name)) {  // ← add karo
            throw new BadRequestException("Name '" + name + "' already exists as a Product");
        }
    }

    // ─── Product UPDATE ke liye ───────────────────────────────  ← naya method
    public void validateUniqueNameForProductUpdate(String name, Long excludeId) {

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Name '" + name + "' already exists as a Category");
        }

        if (subCategoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Name '" + name + "' already exists as a SubCategory");
        }

        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)) {
            throw new BadRequestException("Name '" + name + "' already exists as a Product");
        }
    }
}
