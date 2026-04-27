package com.automobile.ecom.config;


import com.automobile.ecom.dto.AuditLogResponse;
import com.automobile.ecom.entity.AuditLog;

public class AuditMapper {

    public static AuditLogResponse toResponse(AuditLog log) {

        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .role(log.getRole())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}
