package com.automobile.ecom.service;
import com.automobile.ecom.dto.NotificationResponse;
import com.automobile.ecom.dto.PushNotificationRequest;
import com.automobile.ecom.dto.SendNotificationRequest;
import com.automobile.ecom.entity.Notification;
import com.automobile.ecom.entity.NotificationChannel;
import com.automobile.ecom.entity.NotificationStatus;
import com.automobile.ecom.repository.NotificationRepository;
import com.automobile.ecom.repository.UserRepository;
import com.automobile.ecom.security.CustomUserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final SseService sseService;
    private final FcmService fcmService;
    private final UserRepository userRepository;

    /**
     * Send notification directly through API
     */
    public NotificationResponse sendNotification(SendNotificationRequest request) {

        Notification notification = buildNotificationFromRequest(request);

        Notification savedNotification = repository.save(notification);

        pushNotification(savedNotification);
        getUnreadCount(request.getSenderId());

        return mapToResponse(savedNotification);
    }

    /**
     * Get notifications for a user
     */
    public Page<NotificationResponse> getNotifications(UUID Id,int page,int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        return repository
                .findByReceiverIdAndIsDeletedFalse(Id, pageable)
                .map(this::mapToResponse);
    }


    /**
     * Count unread notifications
     */
//    public Long getUnreadCount(UUID Id) {
//
//        return repository.countByReceiverIdAndIsReadFalseAndIsDeletedFalse(Id);
//    }
    public Long getUnreadCount(UUID userId) {
        return repository.countByReceiverIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    public void notifyUnreadCount(UUID userId) {
        Long count = getUnreadCount(userId);
        sseService.sendUnreadCount(userId, count);
    }
    /**
     * Mark notification as read
     */
    public NotificationResponse markAsRead(Long id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return mapToResponse(repository.save(notification));
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(UUID Id) {

        Page<Notification> notifications =
                repository.findByReceiverIdAndIsDeletedFalse(Id, Pageable.unpaged());

        notifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });

        repository.saveAll(notifications.getContent());
    }

    /**
     * Soft delete notification
     */
    public void deleteNotification(Long id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setDeleted(true);
        notification.setDeletedAt(LocalDateTime.now());

        repository.save(notification);
    }

    /**
     * Get all notifications (admin usage)
     */
//    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
//
//        return repository.findByIsDeletedFalse(pageable)
//                .map(this::mapToResponse);
//    }
    public Page<NotificationResponse> getAllNotifications(int page,int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        return repository.findByIsDeletedFalse(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get starred notifications
     */
    public Page<NotificationResponse> getStarredNotifications(UUID Id,int page,int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        return repository
                .findByReceiverIdAndIsStarredTrueAndIsDeletedFalse(Id, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Delete all notifications of a user
     */
    @Transactional
    public void deleteAllNotifications(UUID Id,int page,int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        Page<Notification> notifications =
                repository.findByReceiverIdAndIsDeletedFalse(Id, pageable);

        notifications.forEach(notification -> notification.setDeleted(true));

        repository.saveAll(notifications.getContent());
    }

    /**
     * Update star status
     */
    @Transactional
    public void updateStarStatus(Long id, boolean starred) {

        Notification notification = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setStarred(starred);

        repository.save(notification);
    }

    public void sendToAllAdmins(SendNotificationRequest request) {

        List<UUID> adminIds = userRepository.findAllAdminIds();
        // create this query

        for (UUID adminId : adminIds) {

            request.setReceiverId(adminId);

            Notification notification = buildNotificationFromRequest(request);

            Notification saved = repository.save(notification);

            pushNotification(saved);
        }
    }

    private Notification buildNotificationFromRequest(SendNotificationRequest request) {

        return Notification.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .title(request.getTitle())
                .message(request.getMessage())
                .category(request.getCategory())
                .notificationType(request.getNotificationType())
                .notificationStatus(NotificationStatus.SENT)
                .link(request.getLink())
                .isRead(false)
                .isStarred(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .notificationChannel(request.getNotificationChannel())
                .build();
    }

//    private void pushNotification(Notification notification) {
//
//        try {
//            sseService.send(notification.getReceiverId(), notification);
//
//        } catch (Exception e) {
//
//            log.error("Failed to push notification for user {}", notification.getReceiverId(), e);
//        }
//    }

    private void pushNotification(Notification notification) {

        NotificationChannel channel = notification.getNotificationChannel();

        if (channel == null) {
            // default fallback
            sseService.send(notification.getReceiverId(), notification);
            sendFcm(notification);
            return;
        }

        switch (channel) {

            case SSE -> sseService.send(notification.getReceiverId(), notification);

            case FCM -> sendFcm(notification);

            case BOTH -> {
                sseService.send(notification.getReceiverId(), notification);
                sendFcm(notification);
            }
        }
    }
    private void sendFcm(Notification notification) {

        PushNotificationRequest fcmRequest = PushNotificationRequest.builder()
                .title(notification.getTitle())
                .body(notification.getMessage())
                .data(Map.of(
                        "notificationId", notification.getId().toString(),
                        "type", notification.getNotificationType().name(),
                        "category", notification.getCategory().name(),
                        "senderId", notification.getSenderId() != null
                                ? notification.getSenderId().toString()
                                : "",
                        "receiverId", notification.getReceiverId().toString(),
                        "link", notification.getLink() != null ? notification.getLink() : ""
                ))
                .build();

        // 🔥 send to RECEIVER
        fcmService.sendToUser(notification.getReceiverId(), fcmRequest);
    }

    private NotificationResponse mapToResponse(Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .senderId(notification.getSenderId())
                .receiverId(notification.getReceiverId())
                .title(notification.getTitle())
                .category(notification.getCategory())
                .notificationType(notification.getNotificationType())
                .notificationStatus(notification.getNotificationStatus())
                .message(notification.getMessage())
                .link(notification.getLink())
                .isRead(notification.isRead())
                .isDeleted(notification.isDeleted())
                .isStarred(notification.isStarred())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .notificationChannel(notification.getNotificationChannel())
                .build();
    }
}