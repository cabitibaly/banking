package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.StatusCustomer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer")
public class Customer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCustomer;

    @Column(nullable = false)
    private String nomCustomer;

    @Column(nullable = false)
    private String prenomCustomer;

    @Column(nullable = false)
    private String telephoneCustomer;

    @Column(nullable = false)
    private String numeroCustomer;

    @Column(nullable = false)
    private LocalDateTime dateNaissance;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusCustomer statusCustomer;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "id_user")
    private User user;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "customer")
    private List<KycDocument> kycDocuments = new ArrayList<>();
}
