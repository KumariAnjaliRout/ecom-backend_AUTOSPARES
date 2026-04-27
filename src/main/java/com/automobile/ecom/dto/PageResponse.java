package com.automobile.ecom.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> data;
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private boolean first;
    private boolean last;
}