package com.automobile.ecom.repository;


import com.automobile.ecom.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ─── UNIQUENESS CHECKS ───────────────────────────────────
    boolean existsByPartNumber(String partNumber);
    boolean existsByPartNumberAndIdNot(String partNumber, Long id);

    Optional<Product> findByIdAndIsActiveTrue(Long id);
    @EntityGraph(attributePaths = {"subCategory", "subCategory.category"})
    Page<Product> findByIsActiveTrue(Pageable pageable);


    // Used for exact Part Number search on the frontend
    Optional<Product> findByPartNumberIgnoreCaseAndIsActiveTrue(String partNumber);

    // Used when a user clicks a SubCategory (e.g., "Brake Pads")
    Page<Product> findAllBySubCategoryIdAndIsActiveTrue(Long subCategoryId, Pageable pageable);

    // Used when a user filters by Brand (e.g., "Bosch")
    Page<Product> findAllByCompanyIgnoreCaseAndIsActiveTrue(String company, Pageable pageable);

    // 🚀 Performance Optimized: Joins Category and SubCategory in 1 query
//    @Query(value = """
//        SELECT p FROM Product p
//        JOIN FETCH p.subCategory sc
//        JOIN FETCH sc.category
//        WHERE p.isActive = true
//    """, countQuery = "SELECT count(p) FROM Product p WHERE p.isActive = true")
//    Page<Product> findAllActiveWithHierarchy(Pageable pageable);

    // ─── ATOMIC UPDATES ──────────────────────────────────────

    @Modifying
    @Query("""
        UPDATE Product p
        SET p.stockQuantity = p.stockQuantity - :quantity
        WHERE p.id = :id
        AND p.stockQuantity >= :quantity
    """)
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.rating = :rating WHERE p.id = :id")
    void updateRating(@Param("id") Long id, @Param("rating") BigDecimal rating);

    Page<Product> findAllByCompany(String company, Pageable pageable);
    Page<Product> findAll(Pageable pageable);
//    Page<Product> findAllByCompany(String company, Pageable pageable);

    @Query("""
    SELECT p FROM Product p
    WHERE p.stockQuantity <= :threshold
    AND p.stockQuantity > 0
    AND p.isActive = true
""")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    @Query("""
    SELECT p FROM Product p
    WHERE p.stockQuantity = 0
    AND p.isActive = true
""")
    List<Product> findOutOfStockProducts();

    @Query("""
    SELECT p FROM Product p
    WHERE p.isActive = true
    ORDER BY p.rating DESC
""")
    List<Product> findTopRatedProducts(Pageable pageable);

    @Query("""
    SELECT sc.category.name, SUM(oi.quantity)
    FROM OrderItems oi
    JOIN oi.product p
    JOIN p.subCategory sc
    GROUP BY sc.category.name
    ORDER BY SUM(oi.quantity) DESC
""")
    List<Object[]> findTopSellingCategories(Pageable pageable);
    // ─── UNIQUENESS CHECKS ───────────────────────────────────
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    long countByIsActiveTrue();
    long countByIsActiveFalse();

}