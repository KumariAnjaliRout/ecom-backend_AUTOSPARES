package com.automobile.ecom.repository;


import com.automobile.ecom.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    List<Order> findByUser_Id(UUID userId);
    List<Order> findAllByOrderByIdDesc();
    List<Order> findByCreatedAtAfter(LocalDateTime date);
    Page<Order> findAll(Pageable pageable);

    // Daily / Monthly / Yearly revenue
    @Query("SELECT SUM(o.totalAmount + o.deliveryCost) FROM Order o WHERE o.createdAt >= :start AND o.createdAt <= :end AND o.orderStatus = 'DELIVERED'")
    Double getRevenueBetween(LocalDateTime start, LocalDateTime end);

    // Sales trend
    @Query("SELECT o.createdAt, SUM(o.totalAmount + o.deliveryCost) FROM Order o WHERE o.createdAt >= :start GROUP BY o.createdAt")
    List<Object[]> getSalesTrend(LocalDateTime start);

    boolean existsByAddressId(UUID addressId);

    @Query("""
    SELECT o.paymentMode, SUM(o.totalAmount + o.deliveryCost)
    FROM Order o
    WHERE o.orderStatus = 'DELIVERED'
    GROUP BY o.paymentMode
""")
    List<Object[]> getRevenueByPaymentMode();

//    @Query("""
//    SELECT o.user.id, o.user.username, SUM(o.totalAmount + o.deliveryCost)
//    FROM Order o
//    WHERE o.orderStatus = 'DELIVERED'
//    GROUP BY o.user.id, o.user.username
//    ORDER BY SUM(o.totalAmount + o.deliveryCost) DESC
//""")

    @Query("""
    SELECT o.user.id,
           o.user.username,
           SUM(o.totalAmount + o.deliveryCost),
           COUNT(o.id)
    FROM Order o
    WHERE o.orderStatus = 'DELIVERED'
    GROUP BY o.user.id, o.user.username
    ORDER BY SUM(o.totalAmount + o.deliveryCost) DESC
""")
    List<Object[]> getTopCustomers(Pageable pageable);

    @Query("""
    SELECT p.paymentStatus, COUNT(p)
    FROM Payment p
    GROUP BY p.paymentStatus
""")
    List<Object[]> getPaymentStatusCounts();


}
