package com.jiyuu.banking;

import com.jiyuu.banking.utils.CardNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardNumberGeneratorTest {
    private CardNumberGenerator cardNumberGenerator;

    @BeforeEach
    void setUp() {
        cardNumberGenerator = new CardNumberGenerator();
    }

    @Test
    public void shouldGenerateNumberWith16Chars() {
        String number = cardNumberGenerator.generate();
        assertEquals(16, number.length());
    }

    @Test
    public void shouldReturnTrueForValidNumber() {
        String number = cardNumberGenerator.generate();
        assertTrue(cardNumberGenerator.isValidLuhn(number));
    }

    @Test
    public void shouldReturnFalseForInvalidNumber() {
        String number = cardNumberGenerator.generate();

        int lastDigit = Character.getNumericValue(number.charAt(15));
        int wrongDigit = (lastDigit + 1) % 10;
        String invalidNumber = number.substring(0, 15) + wrongDigit;

        assertFalse(cardNumberGenerator.isValidLuhn(invalidNumber));
    }

    @Test
    public void shouldReturnFalseForInvalidNumberLen() {
        String number = "123456789012345";
        assertFalse(cardNumberGenerator.isValidLuhn(number));
    }

    @Test
    public void shouldReturnFalseForNullNumber() {
        assertFalse(cardNumberGenerator.isValidLuhn(null));
    }
}
