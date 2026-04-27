package com.automobile.ecom.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_logs_search")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;

    private String username;

    private String role;

    private String action;       // e.g. ADD_TO_CART, DELETE_PRODUCT

    private String entityType;   // PRODUCT, USER, ORDER

    private String entityId;

    @Column(length = 1000)
    private String details;

    private LocalDateTime timestamp;
}