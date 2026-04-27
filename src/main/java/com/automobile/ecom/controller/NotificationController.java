package com.automobile.ecom.controller;

import com.automobile.ecom.dto.NotificationResponse;
import com.automobile.ecom.dto.PushNotificationRequest;
import com.automobile.ecom.dto.RegisterDeviceRequest;
import com.automobile.ecom.dto.SendNotificationRequest;
import com.automobile.ecom.entity.DeviceToken;
import com.automobile.ecom.security.CustomUserPrincipal;
import com.automobile.ecom.service.DeviceTokenService;
import com.automobile.ecom.service.FcmService;
import com.automobile.ecom.service.NotificationService;
import com.automobile.ecom.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseService sseService;
    private final NotificationService notificationService;
    private final FcmService fcmService;
    private final DeviceTokenService deviceTokenService;

    /**
     * Subscribe for SSE notifications
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UUID userId = principal.getUserId();
        return sseService.subscribe(userId);
    }

    /**
     * Unsubscribe SSE
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UUID userId = principal.getUserId();
        sseService.disconnect(userId);

        return ResponseEntity.ok("User unsubscribed successfully");
    }

    /**
     * Send notification
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody SendNotificationRequest request) {
        System.out.print("notification");

        return ResponseEntity.ok(notificationService.sendNotification(request));
    }

    /**
     * Get notifications for user
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam int page,
            @RequestParam int size) {
        UUID userId = principal.getUserId();
        return ResponseEntity.ok(notificationService.getNotifications(userId, page,size));
    }

    /**
     * Unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UUID userId = principal.getUserId();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
//
    /**
     * Mark single notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@AuthenticationPrincipal CustomUserPrincipal principal,@PathVariable Long id) {

        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /**
     * Mark all notifications as read
     */

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(@AuthenticationPrincipal CustomUserPrincipal principal) {
        UUID userId = principal.getUserId();
        notificationService.markAllAsRead(userId);

        return ResponseEntity.ok("All notifications marked as read");
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {

        notificationService.deleteNotification(id);

        return ResponseEntity.ok("Notification deleted");
    }

    /**
     * Get all notifications (Admin)
     */
//    @GetMapping("/all")
//    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(Pageable pageable) {
//
//        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
//    }
    @GetMapping("/all")
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications( @RequestParam int page,
                                                                           @RequestParam int size) {

        return ResponseEntity.ok(notificationService.getAllNotifications(page,size));
    }

    /**
     * Get starred notifications
     */
    @GetMapping("/starred")
    public ResponseEntity<Page<NotificationResponse>> getStarredNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam int page,@RequestParam int size) {
        UUID userId = principal.getUserId();
        return ResponseEntity.ok(notificationService.getStarredNotifications(userId,page,size));
    }

    /**
     * Delete all notifications for a user
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam int page,@RequestParam int size) {
        UUID userId = principal.getUserId();
        notificationService.deleteAllNotifications(userId,page,size);

        return ResponseEntity.ok("All notifications deleted successfully");
    }

    /**
     * Update star status
     */
    @PatchMapping("/{id}/star")
    public ResponseEntity<String> updateStarStatus(
            @PathVariable Long id,
            @RequestParam boolean starred) {

        notificationService.updateStarStatus(id, starred);

        return ResponseEntity.ok("Notification star status updated");
    }
//    @PostMapping("/fcm/send")
//    public ResponseEntity<String> send(
//            @AuthenticationPrincipal CustomUserPrincipal principal,
//            @RequestBody PushNotificationRequest request) {
//        UUID userId = principal.getUserId();
//        fcmService.sendToUser(userId, request);
//        return ResponseEntity.ok("Notification triggered");
//    }

    @PostMapping("/register")
    public ResponseEntity<String> registerDevice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody RegisterDeviceRequest request) {
        UUID userId = principal.getUserId();
        deviceTokenService.registerDevice(userId,request);
        return ResponseEntity.ok("Device registered successfully");
    }
    @DeleteMapping("/{fcmToken}")
    public ResponseEntity<String> logoutDevice(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable("fcmToken") String fcmToken) {

        UUID userId = principal.getUserId();

        deviceTokenService.deactivateSingleDevice(userId,fcmToken);

        return ResponseEntity.ok("Logged out successfully");
    }
}