package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "loan_documents")
public class LoanDocument extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLoanDocument;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(nullable = false)
    private String urlDocument;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "id_loan")
    private Loan loan;
}
