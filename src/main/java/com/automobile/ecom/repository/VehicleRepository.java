package com.automobile.ecom.repository;

import com.automobile.ecom.entity.FuelType;
import com.automobile.ecom.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByBrandIgnoreCaseAndModelIgnoreCaseAndYearAndEngineIgnoreCaseAndFuelType(
            String brand, String model, Integer year, String engine, FuelType fuelType
    );

    List<Vehicle> findAllByBrandIgnoreCaseAndIsActiveTrue(String brand);

    List<Vehicle> findAllByBrandIgnoreCaseAndModelIgnoreCaseAndIsActiveTrue(String brand, String model);

    @Query("SELECT DISTINCT v.brand FROM Vehicle v WHERE v.isActive = true ORDER BY v.brand")
    List<String> findAllDistinctBrands();

    @Query("SELECT DISTINCT v.model FROM Vehicle v WHERE v.brand = :brand AND v.isActive = true ORDER BY v.model")
    List<String> findAllModelsByBrand(@Param("brand") String brand);

    @Query("SELECT DISTINCT v.year FROM Vehicle v WHERE v.brand = :brand AND v.model = :model AND v.isActive = true ORDER BY v.year DESC")
    List<Integer> findAllYearsByBrandAndModel(@Param("brand") String brand, @Param("model") String model);
}