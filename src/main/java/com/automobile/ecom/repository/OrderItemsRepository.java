package com.automobile.ecom.repository;


import com.automobile.ecom.entity.OrderItems;
import com.automobile.ecom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

    //  Get items of a specific order
    List<OrderItems> findByOrder_Id(Long orderId);

    //  CART: items not yet placed into an order
    List<OrderItems> findByUser_IdAndOrderIsNull(UUID userId);
    OrderItems findByUser_IdAndProduct_IdAndOrderIsNull(UUID userId, Long productId);

    @Query("""
SELECT i.product.id, i.product.name, SUM(i.quantity)
FROM OrderItems i
WHERE i.order IS NOT NULL
AND i.order.orderStatus IN ('PLACED', 'DISPATCHED', 'DELIVERED')
GROUP BY i.product.id, i.product.name
ORDER BY SUM(i.quantity) DESC
""")
    List<Object[]> getTopSellingProducts(Pageable pageable);

@Query("""
    SELECT p FROM Product p
    WHERE p.id NOT IN (
        SELECT DISTINCT oi.product.id FROM OrderItems oi WHERE oi.order IS NOT NULL
    )
""")
    List<Product> findUnsoldProducts();

}