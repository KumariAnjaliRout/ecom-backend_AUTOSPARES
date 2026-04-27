package com.automobile.ecom.service;


import com.automobile.ecom.dto.*;
import com.automobile.ecom.entity.*;
import com.automobile.ecom.exception.BadRequestException;
import com.automobile.ecom.exception.ResourceNotFoundException;
import com.automobile.ecom.exception.UnauthorizedException;
import com.automobile.ecom.repository.*;
import com.automobile.ecom.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public AddressResponse createAddress(AddressRequest request, CustomUserPrincipal principal) {

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = Address.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .country(request.getCountry())
                .isDefault(false)
                .isDeleted(false)
                .user(user)
                .build();

        return mapToResponse(addressRepository.save(address));
    }

    public List<AddressResponse> getAllAddresses(CustomUserPrincipal principal) {

        return addressRepository.findByUserIdAndIsDeletedFalse(principal.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AddressResponse getAddress(UUID addressId, CustomUserPrincipal principal) {

        Address address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(principal.getUserId())) {
            throw new UnauthorizedException("You are not allowed to access this address");
        }

        return mapToResponse(address);
    }

    public AddressResponse setDefaultAddress(UUID addressId, CustomUserPrincipal principal) {

        UUID userId = principal.getUserId();
        Address newDefault = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!newDefault.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not allowed to modify this address");
        }

        addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId)
                .ifPresent(old -> {
                    old.setDefault(false);
                    addressRepository.save(old);
                });

        newDefault.setDefault(true);

        return mapToResponse(addressRepository.save(newDefault));
    }

    public AddressResponse updateAddress(UUID addressId, AddressRequest request, CustomUserPrincipal principal) {

        Address address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(principal.getUserId())) {
            throw new UnauthorizedException("You are not allowed to update this address");
        }

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPinCode(request.getPinCode());
        address.setCountry(request.getCountry());

        return mapToResponse(addressRepository.save(address));
    }

    public void deleteAddress(UUID addressId, CustomUserPrincipal principal) {

        Address address = addressRepository.findByIdAndIsDeletedFalse(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Ownership check
        if (!address.getUser().getId().equals(principal.getUserId())) {
            throw new UnauthorizedException("You are not allowed to delete this address");
        }

        // SOFT DELETE
        address.setIsDeleted(true);

        // If this was default → unset it
        if (address.isDefault()) {
            address.setDefault(false);

            //  OPTIONAL (BEST PRACTICE): assign another default address
            addressRepository.findByUserIdAndIsDeletedFalse(principal.getUserId())
                    .stream()
                    .filter(a -> !a.getId().equals(addressId))
                    .sorted(Comparator.comparing(Address::getCreatedAt))
                    .findFirst()
                    .ifPresent(a -> {
                        a.setDefault(true);
                        addressRepository.save(a);
                    });
        }

        addressRepository.save(address);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .pinCode(address.getPinCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .isDeleted(address.getIsDeleted())
                .build();
    }
}