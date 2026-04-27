package com.automobile.ecom.repository;

import com.automobile.ecom.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    // ─── EXISTENCE CHECKS ─────────────────────────────────────

    // For CREATE — checks if name exists in the same category
    boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);

    // For UPDATE — checks if name exists in same category but different ID
    boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(String name, Long categoryId, Long id);

    // ─── FETCH BY CATEGORY ────────────────────────────────────

    // Public shop page — only active subcategories under a category
    List<SubCategory> findAllByCategoryIdAndIsActiveTrueOrderByNameAsc(Long categoryId);

    // Admin dashboard — all subcategories under a category
    List<SubCategory> findAllByCategoryIdOrderByNameAsc(Long categoryId);

    // ─── COUNTS (used in CategoryService mapToResponse) ───────

    // Total subcategory count under a category
    long countByCategoryId(Long categoryId);

    // Active subcategory count under a category
    long countByCategoryIdAndIsActiveTrue(Long categoryId);

    // ─── BULK STATUS UPDATE (used on Category toggle) ─────────

    // Fix: bulk deactivate all subcategories under a category
    // Avoids loading all entities into memory just to set isActive = false
    @Query("UPDATE SubCategory s SET s.isActive = false WHERE s.category.id = :categoryId")
    @org.springframework.data.jpa.repository.Modifying
    void deactivateAllByCategoryId(@Param("categoryId") Long categoryId);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<SubCategory> findSubCategoriesWithProductsByCategoryId(@Param("categoryId") Long categoryId);
    @Query("SELECT DISTINCT s FROM SubCategory s " +
            "LEFT JOIN FETCH s.products " +
            "JOIN FETCH s.category " +
            "WHERE s.id = :id")
    Optional<SubCategory> findByIdWithProductsAndCategory(@Param("id") Long id);

    long countByIsActiveTrue();
    long countByIsActiveFalse();


}