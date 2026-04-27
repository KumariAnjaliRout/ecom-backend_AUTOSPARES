package com.automobile.ecom.dto;


import com.automobile.ecom.document.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GlobalSearchResponse {

    private List<ProductDocument> products;
    private List<VehicleDocument> vehicles;
    private List<CategoryDocument> categories;
    private List<SubCategoryDocument> subCategories;
    private List<VehicleDocument> vehicle;
    private List<VehicleCompatibilityDocument> compatibilities;
    private List<CompatibilityDetailsDocument> compatibilityDetails;
}