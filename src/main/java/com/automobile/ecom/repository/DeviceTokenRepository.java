package com.automobile.ecom.repository;

import com.automobile.ecom.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserIdAndActiveTrue(UUID userId);

    Optional<DeviceToken> findByDeviceToken(String token);
    List<DeviceToken> findByUserId(UUID userId);
}