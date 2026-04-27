package com.automobile.ecom.repository;




import com.automobile.ecom.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Long> {

    // Used on CREATE — checks if any category has this name
    boolean existsByNameIgnoreCase(String name);

    // Used on UPDATE — checks if any OTHER category (excluding current id) has this name
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // For the public Shop page
    List<Categories> findAllByIsActiveTrue();

    // For the Admin Dashboard (shows everything)
    List<Categories> findAll();
    @Query("SELECT DISTINCT c FROM Categories c LEFT JOIN FETCH c.subCategories WHERE c.id = :id")
    Optional<Categories> findByIdWithSubCategoriesOnly(@Param("id") Long id);

    long countByIsActiveTrue();
    long countByIsActiveFalse();

}