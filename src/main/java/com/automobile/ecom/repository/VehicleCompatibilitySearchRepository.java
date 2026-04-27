package com.automobile.ecom.repository;

import com.automobile.ecom.document.VehicleCompatibilityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VehicleCompatibilitySearchRepository
        extends ElasticsearchRepository<VehicleCompatibilityDocument, String> {
}