package com.automobile.ecom.dto;

import com.automobile.ecom.entity.DeviceType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeviceTokenRequest {

    private String deviceToken;

    private DeviceType deviceType;

}