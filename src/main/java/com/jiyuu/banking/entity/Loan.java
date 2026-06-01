package com.jiyuu.banking.entity;


import com.jiyuu.banking.enums.LoanStatus;
import com.jiyuu.banking.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "loans")
public class Loan extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLoan;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(nullable = false)
    private Integer duration;

    private BigDecimal interestRate;

    private BigDecimal remainingAmount;

    @Column(nullable = false)
    private LoanStatus loanStatus;

    @Column(nullable = false)
    private LoanType loanType;

    private LocalDate disbursementDate;

    @OneToMany(mappedBy = "loan", fetch = FetchType.LAZY)
    private List<LoanDocument> documents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_customer")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    private Account account;
}
