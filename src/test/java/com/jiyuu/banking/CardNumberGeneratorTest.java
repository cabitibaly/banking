package com.jiyuu.banking;

import com.jiyuu.banking.utils.CardNumberGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardNumberGeneratorTest {

    @Test
    public void shouldGenerateNumberWith16Chars() {
        String number = CardNumberGenerator.generate();
        assertEquals(16, number.length());
    }

    @Test
    public void shouldReturnTrueForValidNumber() {
        String number = CardNumberGenerator.generate();
        assertTrue(CardNumberGenerator.isValidLuhn(number));
    }

    @Test
    public void shouldReturnFalseForInvalidNumber() {
        String number = CardNumberGenerator.generate();

        int lastDigit = Character.getNumericValue(number.charAt(15));
        int wrongDigit = (lastDigit + 1) % 10;
        String invalidNumber = number.substring(0, 15) + wrongDigit;

        assertFalse(CardNumberGenerator.isValidLuhn(invalidNumber));
    }

    @Test
    public void shouldReturnFalseForInvalidNumberLen() {
        String number = "123456789012345";
        assertFalse(CardNumberGenerator.isValidLuhn(number));
    }

    @Test
    public void shouldReturnFalseForNullNumber() {
        assertFalse(CardNumberGenerator.isValidLuhn(null));
    }
}
