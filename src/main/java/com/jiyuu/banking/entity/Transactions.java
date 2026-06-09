package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.Currency;
import com.jiyuu.banking.enums.TransactionStatus;
import com.jiyuu.banking.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transactions extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, unique = true)
    private String transactionRef;

    @Column(nullable = false)
    private BigDecimal amountTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currencyTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @Column(nullable = true)
    private String originalTransactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id_account")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id_account")
    private Account targetAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_card")
    private Card card;
}
