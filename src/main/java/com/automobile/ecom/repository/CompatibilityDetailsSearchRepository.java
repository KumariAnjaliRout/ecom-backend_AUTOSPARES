package com.automobile.ecom.repository;

import com.automobile.ecom.document.CompatibilityDetailsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CompatibilityDetailsSearchRepository
        extends ElasticsearchRepository<CompatibilityDetailsDocument, String> {
}