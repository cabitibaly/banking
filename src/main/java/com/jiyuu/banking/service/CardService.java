package com.jiyuu.banking.service;

import com.jiyuu.banking.dto.CardRequest;
import com.jiyuu.banking.dto.CardResponse;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Card;
import com.jiyuu.banking.enums.CardState;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.CardRepository;
import com.jiyuu.banking.utils.CardNumberGenerator;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;

@Service
@AllArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final AccountService accountService;
    private BCryptPasswordEncoder passwordEncoder;
    private Random random = new Random();

    public CardResponse createCard(CardRequest request) {
        Account account = this.accountService.getAccountByNumber(request.accountNumber());

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
}
