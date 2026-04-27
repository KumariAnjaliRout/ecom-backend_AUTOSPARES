package com.automobile.ecom.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.*;

import com.automobile.ecom.document.*;
import com.automobile.ecom.dto.GlobalSearchResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final ElasticsearchClient client;

    public GlobalSearchResponse search(String keyword) throws IOException {

        return GlobalSearchResponse.builder()
                .products(searchProducts(keyword))
                .vehicles(searchVehicles(keyword))
                .categories(searchCategories(keyword))
                .subCategories(searchSubCategories(keyword))
                .compatibilities(searchCompatibility(keyword))
                .compatibilityDetails(searchCompatibilityDetails(keyword))
                .build();
    }

    // ✅ COMMON QUERY BUILDER
//    private Query buildQuery(String keyword, List<String> fields) {
//
//        if (keyword == null || keyword.isBlank()) {
//            return MatchAllQuery.of(m -> m)._toQuery();
//        }
//
//        return BoolQuery.of(b -> b
//                .should(MultiMatchQuery.of(m -> m
//                        .query(keyword)
//                        .fields(fields)
//                        .fuzziness("AUTO")
//                        .operator(Operator.And)
//                        .boost(2.0f)
//                )._toQuery())
//
//                .should(MultiMatchQuery.of(m -> m
//                        .query(keyword)
//                        .fields(fields)
//                        .type(TextQueryType.PhrasePrefix)
//                        .boost(1.5f)
//                )._toQuery())
//
//                .should(MultiMatchQuery.of(m -> m
//                        .query(keyword)
//                        .fields(fields)
//                        .boost(3.0f)
//                )._toQuery())
//
//                .minimumShouldMatch("1")
//                .build()
//                ._toQuery();
//    }
    private Query buildQuery(String keyword, List<String> fields) {

        if (keyword == null || keyword.isBlank()) {
            return MatchAllQuery.of(m -> m)._toQuery();
        }

        return Query.of(q -> q
                .bool(b -> b

                        // 🔥 FUZZY
                        .should(s -> s
                                .multiMatch(m -> m
                                        .query(keyword)
                                        .fields(fields)
                                        .fuzziness("AUTO")
                                        .operator(Operator.And)
                                        .boost(2.0f)
                                )
                        )

                        // 🔥 PREFIX
                        .should(s -> s
                                .multiMatch(m -> m
                                        .query(keyword)
                                        .fields(fields)
                                        .type(TextQueryType.PhrasePrefix)
                                        .boost(1.5f)
                                )
                        )

                        // 🔥 EXACT BOOST
                        .should(s -> s
                                .multiMatch(m -> m
                                        .query(keyword)
                                        .fields(fields)
                                        .boost(3.0f)
                                )
                        )

                        .minimumShouldMatch("1")
                )
        );
    }

    // ✅ PRODUCTS
    private List<ProductDocument> searchProducts(String keyword) throws IOException {

        SearchResponse<ProductDocument> response = client.search(s -> s
                        .index("products")
                        .query(buildQuery(keyword, List.of(
                                "name",
                                "description",
                                "partNumber",
                                "company",
                                "categoryName",
                                "subCategoryName",
                                "compatibleVehicles"
                        ))),
                ProductDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source())
                .toList();
    }

    // ✅ VEHICLES
    private List<VehicleDocument> searchVehicles(String keyword) throws IOException {

        SearchResponse<VehicleDocument> response = client.search(s -> s
                        .index("vehicles")
                        .query(buildQuery(keyword, List.of(
                                "brand",
                                "model",
                                "fuelType",
                                "transmission"
                        ))),
                VehicleDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source())
                .toList();
    }
    private List<CategoryDocument> searchCategories(String keyword) throws IOException {

        SearchResponse<CategoryDocument> response = client.search(s -> s
                        .index("categories")
                        .query(buildQuery(keyword, List.of(
                                "name"
                        ))),
                CategoryDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source())
                .toList();
    }
    // ✅ SUBCATEGORIES
    private List<SubCategoryDocument> searchSubCategories(String keyword) throws IOException {

        SearchResponse<SubCategoryDocument> response = client.search(s -> s
                        .index("subcategories")
                        .query(buildQuery(keyword, List.of(
                                "name",
                                "categoryName"
                        ))),
                SubCategoryDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source())
                .toList();
    }

    // ✅ COMPATIBILITY
    private List<VehicleCompatibilityDocument> searchCompatibility(String keyword) throws IOException {

        SearchResponse<VehicleCompatibilityDocument> response = client.search(s -> s
                        .index("vehicle_compatibility")
                        .query(buildQuery(keyword, List.of(
                                "productName",
                                "partNumber",
                                "company",
                                "categoryName",
                                "subCategoryName",
                                "vehicleBrand",
                                "model",
                                "fuelType",
                                "engineType"
                        ))),
                VehicleCompatibilityDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source())
                .toList();
    }

    // ✅ DETAILS
    private List<CompatibilityDetailsDocument> searchCompatibilityDetails(String keyword) throws IOException {

        SearchResponse<CompatibilityDetailsDocument> response = client.search(s -> s
                        .index("compatibility_details")
                        .query(buildQuery(keyword, List.of(
                                "engineType",
                                "engineCodes",
                                "brakeType",
                                "motorType",
                                "brand",
                                "model"
                        ))),
                CompatibilityDetailsDocument.class
        );

        return response.hits().hits()
                .stream()
                .map(hit -> hit.source())
                .toList();
    }
}