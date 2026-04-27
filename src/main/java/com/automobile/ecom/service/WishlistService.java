package com.automobile.ecom.service;

import com.automobile.ecom.dto.WishlistItemResponse;
import com.automobile.ecom.dto.WishlistResponse;
import com.automobile.ecom.entity.Product;
import com.automobile.ecom.entity.Wishlist;
import com.automobile.ecom.entity.WishlistItem;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.repository.ProductRepository;
import com.automobile.ecom.repository.WishlistItemRepository;
import com.automobile.ecom.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;



import lombok.extern.slf4j.Slf4j;


import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;

    // ================= GET WISHLIST =================
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(UUID userId){

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElse(null);

        if (wishlist == null) {
            return new WishlistResponse(null, 0, List.of());
        }

        List<WishlistItem> items =
                wishlistItemRepository.findByWishlistIdWithProduct(
                        wishlist.getId()
                );

        return convertToResponse(wishlist, items);
    }

    // ================= ADD PRODUCT =================
    @Transactional
    public void addToWishlist(UUID userId, Long productId){

        Wishlist wishlist = getOrCreateWishlist(userId);

        if (wishlistItemRepository.existsByWishlistIdAndProductId(
                wishlist.getId(), productId)) {

            throw new BadRequestException("Product already exists in wishlist");
        }

        Product product =
                productRepository.findByIdAndIsActiveTrue(productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Product not found"));

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();

        wishlistItemRepository.save(item);

        updateCount(wishlist);
    }

    // ================= REMOVE PRODUCT =================
    @Transactional
    public void removeFromWishlist(UUID userId, Long productId){

        Wishlist wishlist = getOrCreateWishlist(userId);

        WishlistItem item =
                wishlistItemRepository
                        .findByWishlistIdAndProductId(wishlist.getId(), productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Product not found in wishlist"));

        wishlistItemRepository.delete(item);

        updateCount(wishlist);
    }

    // ================= CLEAR WISHLIST =================
    @Transactional
    public void clearWishlist(UUID userId){

        Wishlist wishlist = getOrCreateWishlist(userId);

        wishlistItemRepository.deleteByWishlistId(wishlist.getId());

        updateCount(wishlist);
    }

    // ================= INTERNAL METHODS =================

    private Wishlist getOrCreateWishlist(UUID userId){

        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {

                    Wishlist wishlist = Wishlist.builder()
                            .userId(userId)
                            .itemCount(0)
                            .build();

                    return wishlistRepository.save(wishlist);
                });
    }

    private void updateCount(Wishlist wishlist){

        int count = (int) wishlistItemRepository.countByWishlistId(
                wishlist.getId()
        );

        wishlist.setItemCount(count);

        wishlistRepository.save(wishlist);
    }

    private WishlistResponse convertToResponse(
            Wishlist wishlist,
            List<WishlistItem> items){

        WishlistResponse response = new WishlistResponse();

        response.setWishlistId(wishlist.getId());
        response.setItemCount(wishlist.getItemCount());

        response.setItems(
                items.stream()
                        .map(this::convertToItemResponse)
                        .toList()
        );

        return response;
    }

    private WishlistItemResponse convertToItemResponse(WishlistItem item) {

        WishlistItemResponse response = new WishlistItemResponse();

        Product product = item.getProduct();

        response.setId(item.getId());
        response.setProductId(product != null ? product.getId() : null);

        if (product != null) {

            response.setProductName(product.getName());
            response.setProductImage(product.getPhotoUrl());
            response.setPrice(product.getPrice() != null ? product.getPrice().doubleValue() : null);


            response.setDescription(product.getDescription());
            response.setCompany(product.getCompany());
            response.setStockQuantity(product.getStockQuantity());
            response.setIsActive(product.getIsActive());

            response.setActualPrice(product.getActualPrice() != null ? product.getActualPrice().doubleValue() : null);
            response.setDiscount(product.getDiscount() != null ? product.getDiscount().doubleValue() : null);


            if (product.getSubCategory() != null) {

                response.setSubCategoryName(product.getSubCategory().getName());

                if (product.getSubCategory().getCategory() != null) {
                    response.setCategoryName(product.getSubCategory().getCategory().getName());
                }
            }

        } else {
            // fallback if product deleted
            response.setProductName("Product removed");
        }

        response.setAddedAt(item.getAddedAt());

        return response;
    }


//    private WishlistItemResponse convertToItemResponse(WishlistItem item){
//
//        WishlistItemResponse response = new WishlistItemResponse();
//
//        Product product = item.getProduct();
//
//        response.setId(item.getId());
//        response.setProductId(product != null ? product.getId() : null);
//        response.setProductName(product != null ? product.getName() : "Product removed");
//
//        //  IMPORTANT CHANGE (your requirement)
//        response.setProductImage(product != null ? product.getPhotoUrl() : null);
//
//        response.setPrice(product != null ? product.getPrice().doubleValue() : null);
//
//        response.setAddedAt(item.getAddedAt());
//
//        return response;
//    }

}
