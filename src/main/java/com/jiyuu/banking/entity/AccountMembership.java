package com.jiyuu.banking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account_membership")
public class AccountMembership extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAccountMembership;

    @Column(nullable = false)
    private boolean isPrimary;

    @Column(nullable = false)
    private LocalDateTime dateAdhesion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_account", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_customer", nullable = false)
    private Customer customer;

    @PrePersist
    public void initDateAdhesion() {
        if (this.dateAdhesion == null) {
            this.dateAdhesion = LocalDateTime.now();
        }
    }
}
