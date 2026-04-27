package com.automobile.ecom.repository;

import com.automobile.ecom.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AddressRepository extends JpaRepository<Address, UUID> {

//    List<Address> findByUserId(UUID userId);
//
//    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);
//

        //Get all ACTIVE (not deleted) addresses for user
        List<Address> findByUserIdAndIsDeletedFalse(UUID userId);

        // Get default ACTIVE address
        Optional<Address> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(UUID userId);

        // Get single ACTIVE address
        Optional<Address> findByIdAndIsDeletedFalse(UUID id);
}