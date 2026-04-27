package com.automobile.ecom.controller;

import com.automobile.ecom.dto.*;
import com.automobile.ecom.entity.User;
import com.automobile.ecom.security.CustomUserPrincipal;
import com.automobile.ecom.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> create(
            @RequestBody @Valid AddressRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(addressService.createAddress(request, principal));
    }

    @PatchMapping("/{id}/default")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> setDefault(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(addressService.setDefaultAddress(id, principal));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<AddressResponse>> getAll(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(addressService.getAllAddresses(principal));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> getOne(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(addressService.getAddress(id, principal));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid AddressRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(addressService.updateAddress(id, request, principal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserPrincipal principal

    ) {
        addressService.deleteAddress(id, principal);
        return ResponseEntity.ok("Address deleted successfully");
    }
}