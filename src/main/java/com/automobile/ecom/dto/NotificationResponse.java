package com.automobile.ecom.dto;

import com.automobile.ecom.entity.Category;
import com.automobile.ecom.entity.NotificationChannel;
import com.automobile.ecom.entity.NotificationStatus;
import com.automobile.ecom.entity.NotificationType;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private UUID senderId;

    private UUID receiverId;

    private String title;

    private Category category;

//    private String category;

    private NotificationType notificationType;

//    private String notificationType;

    private NotificationStatus notificationStatus;

    private String message;

    private String link;

    private boolean isRead;

    private boolean isStarred;

    private boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    private LocalDateTime deletedAt;

    private NotificationChannel notificationChannel;
}

