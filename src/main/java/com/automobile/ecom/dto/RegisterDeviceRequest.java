package com.automobile.ecom.dto;

import com.automobile.ecom.entity.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDeviceRequest {
    private UUID userId;
    private String deviceToken;
    private DeviceType deviceType;
}