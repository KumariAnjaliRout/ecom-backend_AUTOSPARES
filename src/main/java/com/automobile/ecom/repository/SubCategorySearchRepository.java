package com.automobile.ecom.repository;

import com.automobile.ecom.document.SubCategoryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SubCategorySearchRepository
        extends ElasticsearchRepository<SubCategoryDocument, String> {
}
