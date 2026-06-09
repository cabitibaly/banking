package com.jiyuu.banking.repository;

import com.jiyuu.banking.entity.Card;
import com.jiyuu.banking.enums.CardState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByExpireAtBeforeAndStateNotIn(LocalDate date, List<CardState> states);
}
