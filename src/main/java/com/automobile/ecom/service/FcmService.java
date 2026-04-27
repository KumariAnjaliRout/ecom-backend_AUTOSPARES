package com.automobile.ecom.service;

import com.automobile.ecom.dto.PushNotificationRequest;
import com.automobile.ecom.entity.DeviceToken;
import com.automobile.ecom.repository.DeviceTokenRepository;
import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final DeviceTokenRepository tokenRepository;

//    public void sendToUser(UUID userId, PushNotificationRequest request) {
//
//        List<DeviceToken> tokens =
//                tokenRepository.findByUserIdAndActiveTrue(userId);
//
//        if (tokens.isEmpty()) {
//            log.warn("No active tokens for user: {}", userId);
//            return;
//        }
//
//        List<String> tokenList = tokens.stream()
//                .map(DeviceToken::getDeviceToken)
//                .toList();
//
//        sendMulticast(tokenList, request);
//    }
    public void sendToUser(UUID userId, PushNotificationRequest request) {
        List<DeviceToken> tokens =
                tokenRepository.findByUserIdAndActiveTrue(userId);
        if (tokens.isEmpty()) {
            log.warn("No active tokens for user: {}", userId);
            return;
        }

        List<String> tokenList = tokens.stream()
                .map(DeviceToken::getDeviceToken)
                .toList();

        sendMulticast(tokenList, request);
    }

    private void sendMulticast(List<String> tokens,
                               PushNotificationRequest request) {

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(buildDataPayload(request)) // 🔥 important
                .setAndroidConfig(getAndroidConfig())
                .setApnsConfig(getApnsConfig())
                .build();


        try {

            BatchResponse response =
                    FirebaseMessaging.getInstance().sendEachForMulticast(message);

            handleResponse(tokens, response);

        }

        catch (Exception e) {
            log.error("FCM send ", e);  // 🔥 shows real error
        }
    }
    private Map<String, String> buildDataPayload(PushNotificationRequest request) {

        Map<String, String> data = new HashMap<>();

        data.put("title", request.getTitle());
        data.put("body", request.getBody());

        if (request.getData() != null) {
            data.putAll(request.getData());
        }

        return data;
    }

    private AndroidConfig getAndroidConfig() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setSound("default")
                        .build())
                .build();
    }

    private ApnsConfig getApnsConfig() {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .setContentAvailable(true)
                        .build())
                .putHeader("apns-priority", "10")
                .build();
    }

    private void handleResponse(List<String> tokens, BatchResponse response) {

        List<SendResponse> responses = response.getResponses();

        for (int i = 0; i < responses.size(); i++) {

            SendResponse res = responses.get(i);

            if (!res.isSuccessful()) {

                String failedToken = tokens.get(i);
                Exception exception = res.getException();

                log.error("Failed token: {}, error: {}", failedToken,
                        exception.getMessage());

                if (exception instanceof FirebaseMessagingException fcmEx) {

                    // Preferred (mid versions)
                    if (fcmEx.getMessagingErrorCode() != null &&
                            fcmEx.getMessagingErrorCode().name().equals("UNREGISTERED")) {

                        deactivateToken(failedToken);
                        return;
                    }

                    // Fallback (older versions)
                    if (fcmEx.getMessage() != null &&
                            fcmEx.getMessage().contains("registration-token-not-registered")) {

                        deactivateToken(failedToken);
                    }
                }
            }
        }
    }

    private void deactivateToken(String token) {
        tokenRepository.findByDeviceToken(token)
                .ifPresent(device -> {
                    device.setActive(false);
                    tokenRepository.save(device);
                });
    }
}