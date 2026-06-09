package com.jiyuu.banking.entity;

import com.jiyuu.banking.enums.CardState;
import com.jiyuu.banking.enums.Network;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "card")
public class Card extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCard;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private LocalDate expireAt;

    private String pin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Network network;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account")
    private Account account;
}
