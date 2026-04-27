//package com.automobile.ecom.service;
//
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import com.automobile.ecom.document.VehicleCompatibilityDocument;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.SearchHit;
//import org.springframework.data.elasticsearch.core.SearchHits;
////import org.springframework.data.jpa.repository.NativeQuery;
//import org.springframework.data.elasticsearch.client.elc.NativeQuery;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class CompatibilitySearchService {
//
//    private final ElasticsearchOperations elasticsearchOperations;
//
//    public Page<VehicleCompatibilityDocument> search(
//            String vehicleBrand,
//            String fuelType,
//            Integer year,
//            String model,
//            String queryText,
//            int page,
//            int size
//    ) {
//
//        BoolQuery.Builder bool = new BoolQuery.Builder();
//
//        // ✅ FILTERS (NO .keyword)
//        if (vehicleBrand != null && !vehicleBrand.isEmpty()) {
//            bool.filter(f -> f.match(m -> m
//                    .field("vehicleBrand")
//                    .query(vehicleBrand)
//            ));
//        }
//
//        if (fuelType != null && !fuelType.isEmpty()) {
//            bool.filter(f -> f.match(m -> m
//                    .field("fuelType")
//                    .query(fuelType)
//            ));
//        }
//
//        if (year != null) {
//            bool.filter(f -> f.term(t -> t
//                    .field("year")
//                    .value(year)
//            ));
//        }
//
//        if (model != null && !model.isEmpty()) {
//            bool.filter(f -> f.match(m -> m
//                    .field("model")
//                    .query(model)
//            ));
//        }
//
//        // ✅ SEARCH TEXT
//        if (queryText != null && !queryText.isEmpty()) {
//            bool.must(m -> m.multiMatch(mm -> mm
//                    .query(queryText)
//                    .fields(
//                            "productName",
//                            "partNumber",
//                            "categoryName",
//                            "subCategoryName"
//                    )
//            ));
//        }
//// ✅ SEARCH TEXT (FUZZY + SMART)
//        if (queryText != null && !queryText.isEmpty()) {
//
//            bool.must(m -> m.bool(b -> b
//                    // 🔥 prefix (case-insensitive)
//                    .should(s -> s.matchPhrasePrefix(mpp -> mpp
//                            .field("productName")
//                            .query(queryText)
//                    ))
//                    // ✅ PREFIX (FOR SHORT TEXT LIKE "B")
//                    .should(s -> s.prefix(p -> p
//                            .field("productName")
//                            .value(queryText.toLowerCase())
//                    ))
//
//                    // ✅ FUZZY MATCH (MAIN FIX)
//                    .should(s -> s.multiMatch(mm -> mm
//                            .query(queryText)
//                            .fields(
//                                    "productName^3",
//                                    "partNumber^5",
//                                    "categoryName^2",
//                                    "subCategoryName"
//                            )
//                            .fuzziness("AUTO")   // 🔥 THIS WAS MISSING
//                    ))
//
//                    // ✅ EXACT MATCH BOOST
//                    .should(s -> s.match(mq -> mq
//                            .field("productName")
//                            .query(queryText)
//                            .boost(2.0f)
//                    ))
//
//                    // ✅ PREFIX MATCH (autocomplete)
//                    .should(s -> s.matchPhrasePrefix(mpp -> mpp
//                            .field("productName")
//                            .query(queryText)
//                    ))
//
//                    .minimumShouldMatch("1")
//            ));
//        }
//        // ✅ BUILD QUERY (REMOVED createdAt SORT ❗)
//        NativeQuery searchQuery = NativeQuery.builder()
//                .withQuery(bool.build()._toQuery())
//                .withPageable(PageRequest.of(page, size))
//                .build();
//
//        // ✅ EXECUTE
//        SearchHits<VehicleCompatibilityDocument> searchHits =
//                elasticsearchOperations.search(searchQuery, VehicleCompatibilityDocument.class);
//
//        List<VehicleCompatibilityDocument> results =
//                searchHits.getSearchHits()
//                        .stream()
//                        .map(SearchHit::getContent)
//                        .toList();
//
//        return new PageImpl<>(results, PageRequest.of(page, size), searchHits.getTotalHits());
//    }
//}

package com.automobile.ecom.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.automobile.ecom.document.VehicleCompatibilityDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompatibilitySearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public Page<VehicleCompatibilityDocument> search(
            String vehicleBrand,
            String fuelType,
            Integer year,
            String model,
            String queryText,
            int page,
            int size
    ) {

        BoolQuery.Builder bool = new BoolQuery.Builder();

        // ✅ FILTERS (keyword fields)
        if (vehicleBrand != null && !vehicleBrand.isBlank()) {
            bool.filter(f -> f.term(t -> t
                    .field("vehicleBrand")
                    .value(vehicleBrand)
            ));
        }

        if (fuelType != null && !fuelType.isBlank()) {
            bool.filter(f -> f.term(t -> t
                    .field("fuelType")
                    .value(fuelType)
            ));
        }

        if (year != null) {
            bool.filter(f -> f.term(t -> t
                    .field("year")
                    .value(year)
            ));
        }

        if (model != null && !model.isBlank()) {
            bool.filter(f -> f.term(t -> t
                    .field("model")
                    .value(model)
            ));
        }

        // ✅ SEARCH (AUTOCOMPLETE + FUZZY + CASE INSENSITIVE)
        if (queryText != null && !queryText.isBlank()) {

            bool.must(m -> m.multiMatch(mm -> mm
                    .query(queryText)
                    .fields(
                            "productName^3",
                            "partNumber^5",
                            "company",
                            "categoryName^2",
                            "subCategoryName"
                    )
                    .fuzziness("AUTO")   // ✅ typo support
            ));

        }

        // ✅ BUILD QUERY (with scoring)
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(bool.build()._toQuery())
                .withPageable(PageRequest.of(page, size))
                .build();

        // ✅ EXECUTE
        SearchHits<VehicleCompatibilityDocument> searchHits =
                elasticsearchOperations.search(searchQuery, VehicleCompatibilityDocument.class);

        List<VehicleCompatibilityDocument> results =
                searchHits.getSearchHits()
                        .stream()
                        .map(SearchHit::getContent)
                        .toList();

        return new PageImpl<>(results, PageRequest.of(page, size), searchHits.getTotalHits());
    }
}