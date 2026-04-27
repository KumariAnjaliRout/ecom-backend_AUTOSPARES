package com.automobile.ecom.repository;


import com.automobile.ecom.entity.Order;
import com.automobile.ecom.entity.Product;
import com.automobile.ecom.entity.Review;
import com.automobile.ecom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndProductAndOrder(User user, Product product, Order order);

    List<Review> findByProduct(Product product);
    List<Review> findByUser(User user);
}
