package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.InstallmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "loan_installment")
public class LoanInstallment extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInstallment;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private BigDecimal principalAmount;

    @Column(nullable = false)
    private BigDecimal interestAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus installmentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loan", nullable = false)
    private Loan loan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transaction")
    private Transactions transaction;
}
