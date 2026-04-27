package com.automobile.ecom.service;


import com.automobile.ecom.config.AuditMapper;
import com.automobile.ecom.dto.AuditLogResponse;
import com.automobile.ecom.dto.PageResponse;
import com.automobile.ecom.entity.AuditLog;
import com.automobile.ecom.repository.AuditRepository;
import com.automobile.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    // FINAL METHOD (UPDATED WITH USERNAME)
    public void logAction(UUID userId,
                          String username,
                          String role,
                          String action,
                          String entityType,
                          String entityId,
                          String details) {

        AuditLog log = AuditLog.builder()
                .userId(userId)
                .username(username)
                .role(role)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        auditRepository.save(log);
    }


    private PageResponse<AuditLogResponse> buildResponse(Page<AuditLog> page) {

        List<AuditLogResponse> data = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AuditLogResponse>builder()
                .data(data)
                .currentPage(page.getNumber())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }


    private AuditLogResponse mapToResponse(AuditLog log) {

        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(
                        log.getUsername() != null
                                ? log.getUsername()
                                : "UNKNOWN_USER"   // fallback for old data
                )
                .role(log.getRole())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }


    public PageResponse<AuditLogResponse> getAllLogs(Pageable pageable) {
        return buildResponse(
                auditRepository.findAllByOrderByTimestampDesc(pageable)
        );
    }


    public PageResponse<AuditLogResponse> getLogsByUser(UUID userId, Pageable pageable) {
        return buildResponse(
                auditRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
        );
    }

    // BY ACTION
    public PageResponse<AuditLogResponse> getLogsByAction(String action, Pageable pageable) {
        return buildResponse(
                auditRepository.findByActionOrderByTimestampDesc(action, pageable)
        );
    }

    // BY ENTITY
    public PageResponse<AuditLogResponse> getLogsByEntity(String entityType, Pageable pageable) {
        return buildResponse(
                auditRepository.findByEntityTypeOrderByTimestampDesc(entityType, pageable)
        );
    }

    //  LOGIN HISTORY
    public PageResponse<AuditLogResponse> getLoginHistory(Pageable pageable) {

        List<String> actions = List.of(
                "LOGIN_SUCCESS",
                "LOGIN_FAILED",
                "LOGOUT"
        );

        return buildResponse(
                auditRepository.findByActionInOrderByTimestampDesc(actions, pageable)
        );
    }
}
