package com.jiyuu.banking;

import com.jiyuu.banking.utils.AccountNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountNumberGeneratorTest {
    private AccountNumberGenerator accountNumberGenerator;

    @BeforeEach
    void setUp() {
        accountNumberGenerator = new AccountNumberGenerator();
    }

    @Test
    public void shouldGenerateNumberWithBF() {
        String number = accountNumberGenerator.generate();
        assertTrue(number.startsWith("BF"));
    }

    @Test
    public void shouldGenerateNumberWith27Chars() {
        String number = accountNumberGenerator.generate();
        assertEquals(27, number.length());
    }

    @Test
    public void shouldGenerateHaveDigitsAferBF() {
        String number = accountNumberGenerator.generate();
        assertTrue(number.substring(2).matches("\\d{25}"));
    }

    @Test
    public void shouldGenerateValidNumber() {
        String number = accountNumberGenerator.generate();

        String rearranged = number.substring(4) + number.substring(0, 4);

        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                numeric.append(c);
            }
        }

        int remainder = mod97(numeric.toString());
        assertEquals(remainder, 1);
    }

    private static int mod97(String numericString) {
        int remainder = 0;
        for (char c : numericString.toCharArray()) {
            remainder = (remainder * 10 + (c - '0')) % 97;
        }
        return remainder;
    }
}
