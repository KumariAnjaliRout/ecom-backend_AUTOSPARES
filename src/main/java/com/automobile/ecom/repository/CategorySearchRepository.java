package com.automobile.ecom.repository;


import com.automobile.ecom.document.CategoryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CategorySearchRepository
        extends ElasticsearchRepository<CategoryDocument, String> {
}