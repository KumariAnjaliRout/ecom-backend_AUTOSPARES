package com.automobile.ecom.service;


import com.automobile.ecom.entity.Notification;
import com.automobile.ecom.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@RequiredArgsConstructor
@Service
public class SseService {
    private final NotificationRepository notificationRepository;
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<UUID, SseEmitter>();

    public SseEmitter subscribe(UUID userId) {

        SseEmitter emitter = new SseEmitter(0L); // no timeout

        emitters.put(userId, emitter);
//    presenceService.registerUser(userId);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Subscription successful"));
        } catch (Exception e) {
            emitters.remove(userId);
        }

        // Fetch missed notifications
        List<Notification> missed =
                notificationRepository.findByReceiverIdAndIsDeletedFalseAndIsReadFalse(userId);

        if (!missed.isEmpty()) {
            missed.forEach(notification -> send(userId, notification));
        }

        return emitter;
    }
    public void disconnect(UUID userId) {

        emitters.remove(userId);

//        presenceService.removeUser(userId);
    }


    public void send(UUID userId, Notification notification) {

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));

        } catch (Exception e) {
            emitters.remove(userId);
        }
    }
    public void sendUnreadCount(UUID userId, Long count) {

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name("unread-count")
                    .data(count));
        } catch (Exception e) {
            emitters.remove(userId);
        }
    }
}
