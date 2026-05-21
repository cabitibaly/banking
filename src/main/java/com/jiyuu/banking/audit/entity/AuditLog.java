package com.jiyuu.banking.audit.entity;

import com.jiyuu.banking.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditLog;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String Entity;

    @Column(nullable = false)
    private String idEntity;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private String IpAddress;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

}
