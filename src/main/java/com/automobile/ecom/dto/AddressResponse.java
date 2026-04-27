package com.automobile.ecom.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AddressResponse {

    private UUID id;
    private String fullName;
    private String phoneNumber;
    private String street;
    private String city;
    private String state;
    private String pinCode;
    private String country;
    private boolean isDefault;
    private boolean isDeleted;
}