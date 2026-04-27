package com.automobile.ecom.controller;

import com.automobile.ecom.dto.AuditLogResponse;
import com.automobile.ecom.dto.PageResponse;
import com.automobile.ecom.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    // ALL LOGS
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAllLogs(
            Pageable pageable) {

        return ResponseEntity.ok(auditService.getAllLogs(pageable));
    }

    //  BY USER
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getLogsByUser(
            @PathVariable UUID userId,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditService.getLogsByUser(userId, pageable)
        );
    }

    // BY ACTION
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getLogsByAction(
            @PathVariable String action,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditService.getLogsByAction(action, pageable)
        );
    }

    //  BY ENTITY
    @GetMapping("/entity/{entityType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getLogsByEntity(
            @PathVariable String entityType,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditService.getLogsByEntity(entityType, pageable)
        );
    }

    //  LOGIN HISTORY (KEY FEATURE)
    @GetMapping("/logins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogResponse>> getLoginHistory(
            Pageable pageable) {
        return ResponseEntity.ok(
                auditService.getLoginHistory(pageable)
        );
    }
}
