package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.KycStatus;
import com.jiyuu.banking.enums.KycType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "kyc_documents")
public class KycDocument extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKycDocument;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private KycType kycType;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "id_customer")
    private Customer customer;
}
