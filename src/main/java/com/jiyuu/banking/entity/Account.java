package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.AccountType;
import com.jiyuu.banking.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, unique = true)
    private String numeroAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal soldeAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal decouvert;

    @Column(nullable = false)
    private boolean isDecouvert;

    @Column(nullable = false)
    private Currency currencyAccount;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AccountMembership> accountMembershipList = new ArrayList<>();

}
