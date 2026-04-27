package com.automobile.ecom.repository;


import com.automobile.ecom.entity.CompatibilityDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompatibilityDetailsRepository extends JpaRepository<CompatibilityDetails, Long> {
    Optional<CompatibilityDetails> findByVehicleCompatibilityId(Long vehicleCompatibilityId);
    boolean existsByVehicleCompatibilityId(Long vehicleCompatibilityId);
}