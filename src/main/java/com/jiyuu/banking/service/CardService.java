package com.jiyuu.banking.service;

import com.jiyuu.banking.dto.CardRequest;
import com.jiyuu.banking.dto.CardResponse;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Card;
import com.jiyuu.banking.enums.CardState;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CardRepository;
import com.jiyuu.banking.utils.CardNumberGenerator;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private Random random = new Random();

    public CardResponse createCard(CardRequest request) {
        Account account = this.accountRepository.findBynumeroAccount(request.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String cvv = String.format("%03d", random.nextInt(1000));
        String cvvHash = this.passwordEncoder.encode(cvv);

        LocalDate expireAt = LocalDate.now().plusYears(3);
        String carNumber = CardNumberGenerator.generate();

        Card card = Card.builder()
                .cardNumber(carNumber)
                .cvv(cvvHash)
                .expireAt(expireAt)
                .account(account)
                .network(request.network())
                .state(CardState.PENDING)
                .build();

        card = this.cardRepository.save(card);
        return CardResponse.of(card, cvv);
    }

    public void activateCard(String cardNumber, String pin) {
        Card card = this.cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (card.getState() != CardState.PENDING) {
            throw new ValidationException("Impossible d'effectuer l'opération");
        }

        String pinHash = this.passwordEncoder.encode(pin);

        card.setState(CardState.ACTIVE);
        card.setPin(pinHash);
        this.cardRepository.save(card);
    }

    public void changeState(String cardNumber, CardState state) {
        Card card = this.cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (!card.getState().isAllowedTransition(state)) {
            throw new ValidationException("Impossible de passer de l'état " + card.getState().name() + " à " + state.name());
        }

        card.setState(state);
        this.cardRepository.save(card);
    }

    public Card validCard(String cardNumber, String pin, String cvv, LocalDate expirationDate) {
        Card card = this.cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        if (card.getState() != CardState.ACTIVE) {
            throw new ValidationException("Transaction non autorisée");
        }

        if (!expirationDate.isEqual(card.getExpireAt())) {
            throw new ValidationException("Transaction non autorisée");
        }

        if (!passwordEncoder.matches(cvv, card.getCvv())) {
            throw new ValidationException("Transaction non autorisée");
        }

        if (!passwordEncoder.matches(pin, card.getPin())) {
            throw new ValidationException("Transaction non autorisée");
        }

        return card;
    }

    public void markCardsAsExpired() {
        List<Card> cards = this.cardRepository
                .findByExpireAtBeforeAndStateNotIn(
                        LocalDate.now(),
                        List.of(CardState.EXPIRED, CardState.CANCELED)
                );

        cards.forEach(card -> card.setState(CardState.EXPIRED));
        this.cardRepository.saveAll(cards);
    }
}
