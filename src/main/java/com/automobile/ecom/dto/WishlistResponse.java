package com.automobile.ecom.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {

    private Long wishlistId;
    private Integer itemCount;
    private List<WishlistItemResponse> items;
}

