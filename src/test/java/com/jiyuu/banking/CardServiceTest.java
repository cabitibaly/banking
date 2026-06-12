package com.jiyuu.banking;

import com.jiyuu.banking.dto.CardRequest;
import com.jiyuu.banking.dto.CardResponse;
import com.jiyuu.banking.entity.Account;
import com.jiyuu.banking.entity.Card;
import com.jiyuu.banking.enums.AccountStatus;
import com.jiyuu.banking.enums.CardState;
import com.jiyuu.banking.enums.Network;
import com.jiyuu.banking.exception.ResourceNotFoundException;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.repository.AccountRepository;
import com.jiyuu.banking.repository.CardRepository;
import com.jiyuu.banking.service.CardService;
import com.jiyuu.banking.utils.CardNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private Random random;

    @InjectMocks
    private CardService cardService;

    @Test
    public void shouldReturnAccountNotFound() {
        CardRequest request = new CardRequest(
                "BF8941844103799996966381059",
                Network.VISA
        );

        when(
                accountRepository
                        .findBynumeroAccount("BF8941844103799996966381059")
        ).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cardService.createCard(request)
        );
    }

    @Test
    public void shouldReturnCardResponse() {
        CardRequest request = new CardRequest(
                "BF8941844103799996966381051",
                Network.VISA
        );

        Account account = Account.builder()
                .accountStatus(AccountStatus.ACTIVE)
                .numeroAccount("BF8941844103799996966381051")
                .build();

        when(accountRepository.findBynumeroAccount("BF8941844103799996966381051"))
                .thenReturn(Optional.of(account));

        when(cardNumberGenerator.generate())
                .thenReturn("9442698017382506");

        when(random.nextInt(1000)).thenReturn(123);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedvalue");

        Card savedCard = Card.builder()
                .idCard(12L)
                .cardNumber("9442698017382506")
                .cvv("123")
                .network(Network.VISA)
                .state(CardState.ACTIVE)
                .expireAt(LocalDate.now())
                .build();

        when(cardRepository.save(any(Card.class)))
                .thenReturn(savedCard);

        CardResponse response = cardService.createCard(request);

        assertNotNull(response);
        assertEquals("9442698017382506", response.cardNumber());
        assertNotNull(response.cvv());
        assertEquals(3, response.cvv().length());
    }

    @Test
    public void shouldReturnCardNotFound() {
        String cardNumber = "9442698017382500";
        String pin = "1234";

        when(cardRepository.findByCardNumber("9442698017382500"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cardService.activateCard(cardNumber, pin)
        );
    }

    @Test
    public void shouldReturnActiveAlreadyActive() {
        String cardNumber = "9442698017382506";
        String pin = "1234";

        Card card = Card.builder()
                .idCard(12L)
                .cardNumber("9442698017382506")
                .cvv("123")
                .network(Network.VISA)
                .state(CardState.ACTIVE)
                .expireAt(LocalDate.now())
                .build();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        assertThrows(
                ValidationException.class,
                () -> cardService.activateCard(cardNumber, pin)
        );
    }

    @Test
    public void shouldActiveCard() {
        String cardNumber = "9442698017382506";
        String pin = "1234";

        Card card = Card.builder()
                .idCard(12L)
                .cardNumber("9442698017382506")
                .cvv("123")
                .network(Network.VISA)
                .state(CardState.PENDING)
                .expireAt(LocalDate.now())
                .build();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedvalue");

        cardService.activateCard(cardNumber, pin);

        verify(cardRepository).save(card);
        assertEquals(CardState.ACTIVE, card.getState());
        assertNotNull(card.getPin());
    }

    @Test
    public void shouldThrowWhenCardNotFoundOnChangeState() {
        String cardNumber = "9442698017382500";

        when(cardRepository.findByCardNumber("9442698017382500"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cardService.changeState(cardNumber, CardState.ACTIVE)
        );
    }

    @Test
    public void shouldReturnInvalidTransition() {
        String cardNumber = "9442698017382506";
        String pin = "1234";

        Card card = Card.builder()
                .idCard(12L)
                .cardNumber("9442698017382506")
                .cvv("123")
                .network(Network.VISA)
                .state(CardState.ACTIVE)
                .expireAt(LocalDate.now())
                .build();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        assertThrows(
                ValidationException.class,
                () -> cardService.changeState(cardNumber, CardState.ACTIVE)
        );
    }

    @Test
    public void shouldChangeCardState() {
        String cardNumber = "9442698017382506";

        Card card = Card.builder()
                .idCard(12L)
                .cardNumber("9442698017382506")
                .cvv("123")
                .network(Network.VISA)
                .state(CardState.ACTIVE)
                .expireAt(LocalDate.now())
                .build();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        cardService.changeState(cardNumber, CardState.BLOCKED);

        verify(cardRepository).save(card);
        assertEquals(CardState.BLOCKED, card.getState());
    }

    @Test
    public void shouldThrowWhenCardNotFoundOnValidCard() {
        String cardNumber = "9442698017382506";
        String cvv = "123";
        String pin = "1234";
        LocalDate expirationDate = LocalDate.of(2026, 1, 1);

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cardService.validCard(cardNumber, pin, cvv, expirationDate)
        );
    }

    @Test
    public void shouldThrowWhenCardNotActiveOnValidCard() {
        String cardNumber = "9442698017382506";
        String cvv = "123";
        String pin = "1234";
        LocalDate expirationDate = LocalDate.now();

        Card card = buildActiveCard();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        assertThrows(
                ValidationException.class,
                () -> cardService.validCard(cardNumber, pin, cvv, expirationDate)
        );
    }

    @Test
    public void shouldThrowWhenCardExpiredOnValidCard() {
        String cardNumber = "9442698017382506";
        String cvv = "123";
        String pin = "1234";
        LocalDate expirationDate = LocalDate.of(2027, 1, 1);

        Card card = buildActiveCard();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        assertThrows(
                ValidationException.class,
                () -> cardService.validCard(cardNumber, pin, cvv, expirationDate)
        );
    }

    @Test
    public void shouldThrowWhenCvvIncorrectOnValidCard() {
        String cardNumber = "9442698017382506";
        String cvv = "123";
        String pin = "1234";
        LocalDate expirationDate = LocalDate.now();

        Card card = buildActiveCard();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        when(passwordEncoder.matches("123", "$2a$10$hashedvalue"))
                .thenReturn(Boolean.FALSE);

        assertThrows(
                ValidationException.class,
                () -> cardService.validCard(cardNumber, pin, cvv, expirationDate)
        );
    }

    @Test
    public void shouldThrowWhenPinIncorrectOnValidCard() {
        String cardNumber = "9442698017382506";
        String cvv = "123";
        String pin = "1234";
        LocalDate expirationDate = LocalDate.now();

        Card card = buildActiveCard();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        when(passwordEncoder.matches("123", "$2a$10$hashedvalue"))
                .thenReturn(Boolean.TRUE);

        when(passwordEncoder.matches("1234", "$2a$10$hashedvalue"))
                .thenReturn(Boolean.FALSE);

        assertThrows(
                ValidationException.class,
                () -> cardService.validCard(cardNumber, pin, cvv, expirationDate)
        );
    }

    @Test
    public void shouldReturnCardOnValidCard() {
        String cardNumber = "9442698017382506";
        String cvv = "123";
        String pin = "1234";
        LocalDate expirationDate = LocalDate.now();

        Card card = buildActiveCard();

        when(cardRepository.findByCardNumber("9442698017382506"))
                .thenReturn(Optional.of(card));

        when(passwordEncoder.matches("123", "$2a$10$hashedvalue"))
                .thenReturn(Boolean.TRUE);

        when(passwordEncoder.matches("1234", "$2a$10$hashedvalue"))
                .thenReturn(Boolean.TRUE);

        Card cardEntity = cardService.validCard(cardNumber, pin, cvv, expirationDate);

        assertEquals(cardEntity, card);
    }

    private Card buildActiveCard() {
        return Card.builder()
                .idCard(12L)
                .cardNumber("9442698017382506")
                .cvv("$2a$10$hashedvalue")
                .pin("$2a$10$hashedvalue")
                .network(Network.VISA)
                .state(CardState.ACTIVE)
                .expireAt(LocalDate.now())
                .build();
    }

}
