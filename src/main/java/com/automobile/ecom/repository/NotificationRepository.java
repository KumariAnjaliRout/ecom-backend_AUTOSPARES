package com.automobile.ecom.repository;

import com.automobile.ecom.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverIdAndIsDeletedFalseAndIsReadFalse(UUID receiverId);
    Page<Notification> findByReceiverIdAndIsDeletedFalse(UUID receiverId, Pageable pageable);


    Long countByReceiverIdAndIsReadFalseAndIsDeletedFalse(UUID receiverId);
    Page<Notification> findByReceiverIdAndCategoryAndDeletedFalse(
            Long receiverId,
            Enum category,
            Pageable pageable
    );
    Page<Notification> findByIsDeletedFalse(Pageable pageable);
    Page<Notification> findByIsStarredTrueAndIsDeletedFalse(Pageable pageable);
    Page<Notification> findByReceiverIdAndIsStarredTrueAndIsDeletedFalse(UUID receiverId, Pageable pageable);
}



