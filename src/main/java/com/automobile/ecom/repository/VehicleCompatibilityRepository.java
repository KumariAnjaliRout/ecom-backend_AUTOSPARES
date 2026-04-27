package com.automobile.ecom.repository;

import com.automobile.ecom.entity.FuelType;
import com.automobile.ecom.entity.VehicleCompatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleCompatibilityRepository extends JpaRepository<VehicleCompatibility, Long> {

    boolean existsByProductIdAndVehicleId(Long productId, Long vehicleId);

    List<VehicleCompatibility> findAllByProductId(Long productId);

    @Query("""
        SELECT vc FROM VehicleCompatibility vc
        JOIN FETCH vc.product p
        WHERE vc.vehicle.id = :vehicleId
        AND p.isActive = true
    """)
    List<VehicleCompatibility> findAllProductsByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("""
        SELECT vc FROM VehicleCompatibility vc
        JOIN FETCH vc.product p
        JOIN vc.vehicle v
        WHERE v.brand = :brand
        AND v.model = :model
        AND v.year = :year
        AND p.isActive = true
    """)
    List<VehicleCompatibility> findCompatibleProducts(
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("year") Integer year
    );

    // ─── FILTER STEP 1: distinct brands ──────────────────────
    @Query("""
        SELECT DISTINCT v.brand
        FROM VehicleCompatibility vc
        JOIN vc.vehicle v
        JOIN vc.product p
        WHERE p.isActive = true
        ORDER BY v.brand
    """)
    List<String> findDistinctBrands();

    // ─── FILTER STEP 2: distinct fuelTypes by brand ───────────
    @Query("""
        SELECT DISTINCT v.fuelType
        FROM VehicleCompatibility vc
        JOIN vc.vehicle v
        JOIN vc.product p
        WHERE v.brand = :brand
        AND p.isActive = true
    """)
    List<FuelType> findDistinctFuelTypesByBrand(@Param("brand") String brand);

    // ─── FILTER STEP 3: distinct years by brand + fuelType ────
// ─── FILTER STEP 3: distinct years by brand + fuelType ────
    @Query("""
    SELECT DISTINCT v.year
    FROM VehicleCompatibility vc
    JOIN vc.vehicle v
    JOIN vc.product p
    WHERE LOWER(v.brand) = LOWER(:brand)
    AND v.fuelType = :fuelType
    AND p.isActive = true
    ORDER BY v.year DESC
""")
    List<Integer> findDistinctYearsByBrandAndFuelType(
            @Param("brand") String brand,
            @Param("fuelType") FuelType fuelType
    );


    @Query("""
    SELECT DISTINCT v.model
    FROM VehicleCompatibility vc
    JOIN vc.vehicle v
    JOIN vc.product p
    WHERE LOWER(v.brand) = LOWER(:brand)
    AND v.fuelType = :fuelType
    AND v.year = :year
    AND p.isActive = true
    ORDER BY v.model
""")
    List<String> findDistinctModelsByBrandFuelTypeAndYear(
            @Param("brand") String brand,
            @Param("fuelType") FuelType fuelType,
            @Param("year") Integer year
    );

    // ─── FILTER STEP 5 ────
    @Query("""
    SELECT vc FROM VehicleCompatibility vc
    JOIN FETCH vc.product p
    JOIN vc.vehicle v
    WHERE LOWER(v.brand) = LOWER(:brand)
    AND v.fuelType = :fuelType
    AND v.year = :year
    AND v.model = :model
    AND p.isActive = true
""")
    List<VehicleCompatibility> findProductsByFilter(
            @Param("brand") String brand,
            @Param("fuelType") FuelType fuelType,
            @Param("year") Integer year,
            @Param("model") String model
    );

    // ─── FINAL ────
    @Query("""
    SELECT vc FROM VehicleCompatibility vc
    JOIN FETCH vc.product p
    JOIN vc.vehicle v
    WHERE LOWER(v.brand) = LOWER(:brand)
    AND v.fuelType = :fuelType
    AND v.year = :year
    AND v.model = :model
    AND v.engine = :engine
    AND p.isActive = true
""")
    List<VehicleCompatibility> findProductsWithEngine(
            @Param("brand") String brand,
            @Param("fuelType") FuelType fuelType,
            @Param("year") Integer year,
            @Param("model") String model,
            @Param("engine") String engine
    );
}



//package com.automobile.ecom.repository;
//
//
//import com.automobile.ecom.entity.VehicleCompatibility;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface VehicleCompatibilityRepository extends JpaRepository<VehicleCompatibility, Long> {
//
//    boolean existsByProductIdAndVehicleId(Long productId, Long vehicleId);
//
//    List<VehicleCompatibility> findAllByProductId(Long productId);
//
//    @Query("""
//        SELECT vc FROM VehicleCompatibility vc
//        JOIN FETCH vc.product p
//        WHERE vc.vehicle.id = :vehicleId
//        AND p.isActive = true
//    """)
//    List<VehicleCompatibility> findAllProductsByVehicleId(@Param("vehicleId") Long vehicleId);
//
//    @Query("""
//        SELECT vc FROM VehicleCompatibility vc
//        JOIN FETCH vc.product p
//        JOIN vc.vehicle v
//        WHERE v.brand = :brand
//        AND v.model = :model
//        AND v.year = :year
//        AND p.isActive = true
//    """)
//    List<VehicleCompatibility> findCompatibleProducts(
//            @Param("brand") String brand,
//            @Param("model") String model,
//            @Param("year") Integer year
//    );
//
//    // ─── FILTER STEP 1: distinct brands that have compatibility data ──────────
//    @Query("""
//        SELECT DISTINCT v.brand
//        FROM VehicleCompatibility vc
//        JOIN vc.vehicle v
//        JOIN vc.product p
//        WHERE p.isActive = true
//        ORDER BY v.brand
//    """)
//    List<String> findDistinctBrands();
//
//    // ─── FILTER STEP 2: distinct fuelTypes by brand ───────────────────────────
//    @Query("""
//        SELECT DISTINCT CAST(v.fuelType AS string)
//        FROM VehicleCompatibility vc
//        JOIN vc.vehicle v
//        JOIN vc.product p
//        WHERE v.brand = :brand
//        AND p.isActive = true
//        ORDER BY CAST(v.fuelType AS string)
//    """)
//    List<String> findDistinctFuelTypesByBrand(@Param("brand") String brand);
//
//    // ─── FILTER STEP 3: distinct years by brand + fuelType ───────────────────
//    @Query("""
//        SELECT DISTINCT v.year
//        FROM VehicleCompatibility vc
//        JOIN vc.vehicle v
//        JOIN vc.product p
//        WHERE v.brand = :brand
//        AND CAST(v.fuelType AS string) = :fuelType
//        AND p.isActive = true
//        ORDER BY v.year DESC
//    """)
//    List<Integer> findDistinctYearsByBrandAndFuelType(
//            @Param("brand") String brand,
//            @Param("fuelType") String fuelType
//    );
//
//    // ─── FILTER STEP 4: distinct models by brand + fuelType + year ───────────
//    @Query("""
//        SELECT DISTINCT v.model
//        FROM VehicleCompatibility vc
//        JOIN vc.vehicle v
//        JOIN vc.product p
//        WHERE v.brand = :brand
//        AND CAST(v.fuelType AS string) = :fuelType
//        AND v.year = :year
//        AND p.isActive = true
//        ORDER BY v.model
//    """)
//    List<String> findDistinctModelsByBrandFuelTypeAndYear(
//            @Param("brand") String brand,
//            @Param("fuelType") String fuelType,
//            @Param("year") Integer year
//    );
//    @Query("""
//        SELECT vc FROM VehicleCompatibility vc
//        JOIN FETCH vc.product p
//        JOIN vc.vehicle v
//        WHERE v.brand = :brand
//        AND CAST(v.fuelType AS string) = :fuelType
//        AND v.year = :year
//        AND v.model = :model
//        AND v.engine = :engine
//        AND p.isActive = true
//    """)
//    List<VehicleCompatibility> findProductsWithEngine(
//            @Param("brand") String brand,
//            @Param("fuelType") String fuelType,
//            @Param("year") Integer year,
//            @Param("model") String model,
//            @Param("engine") String engine
//    );
//
//    // ─── FINAL: products by brand + fuelType + year + model ──────────────────
//    @Query("""
//        SELECT vc FROM VehicleCompatibility vc
//        JOIN FETCH vc.product p
//        JOIN vc.vehicle v
//        WHERE v.brand = :brand
//        AND CAST(v.fuelType AS string) = :fuelType
//        AND v.year = :year
//        AND v.model = :model
//        AND p.isActive = true
//    """)
//    List<VehicleCompatibility> findProductsByFilter(
//            @Param("brand") String brand,
//            @Param("fuelType") String fuelType,
//            @Param("year") Integer year,
//            @Param("model") String model
//    );
//
//
//}
