package com.automobile.ecom.repository;

import com.automobile.ecom.document.VehicleDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VehicleSearchRepository
        extends ElasticsearchRepository<VehicleDocument, String> {
}