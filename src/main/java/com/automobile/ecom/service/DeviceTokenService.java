package com.automobile.ecom.service;

import com.automobile.ecom.dto.RegisterDeviceRequest;
import com.automobile.ecom.entity.DeviceToken;
import com.automobile.ecom.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenService {

    private final DeviceTokenRepository repository;

    public void registerDevice(UUID userId,RegisterDeviceRequest request) {

        Optional<DeviceToken> existingToken =
                repository.findByDeviceToken(request.getDeviceToken());

        if (existingToken.isPresent()) {

            // ✅ Token already exists → update user/device info
            DeviceToken token = existingToken.get();

            token.setUserId(userId);
            token.setDeviceType(request.getDeviceType());
            token.setActive(true);

            repository.save(token);

            log.info("Updated existing device token for user: {}", request.getUserId());
            return;
        }

        // ✅ New token → insert
        DeviceToken newToken = DeviceToken.builder()
                .userId(userId)
                .deviceToken(request.getDeviceToken())
                .deviceType(request.getDeviceType())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(newToken);

        log.info("Registered new device token for user: {}", request.getUserId());
    }

    public void deactivateSingleDevice(UUID userId, String deviceToken) {

        repository.findByDeviceToken(deviceToken)
                .ifPresent(device -> {

                    // ✅ Security check (important)
                    if (!device.getUserId().equals(userId)) {
                        throw new RuntimeException("Unauthorized device");
                    }

                    device.setActive(false);
                    repository.save(device);

                    log.info("Device logged out: {}", deviceToken);
                });
    }
}