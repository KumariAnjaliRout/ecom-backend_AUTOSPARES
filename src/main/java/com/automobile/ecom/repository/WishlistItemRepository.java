package com.automobile.ecom.repository;

import com.automobile.ecom.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    boolean existsByWishlistIdAndProductId(Long wishlistId, Long productId);

    void deleteByWishlistId(Long wishlistId);

    long countByWishlistId(Long wishlistId);

    // IMPORTANT (for image + product data)
    @Query("""
        SELECT wi
        FROM WishlistItem wi
        JOIN FETCH wi.product
        WHERE wi.wishlist.id = :wishlistId
    """)
    List<WishlistItem> findByWishlistIdWithProduct(Long wishlistId);
}