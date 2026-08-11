package com.jiyuu.banking.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {
    private static final int TOTAL_LENGTH = 16;
    private static final int BASE_LENGTH  = 15; // 15 chiffres + 1 checkdigit Luhn

    private static final SecureRandom random = new SecureRandom();

    public String generate() {
        return buildLuhnNumber();
    }

    private String buildLuhnNumber() {
        StringBuilder sb = new StringBuilder(TOTAL_LENGTH);
        sb.append(1 + random.nextInt(9));
        for (int i = 1; i < BASE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        int checkDigit = computeLuhnCheckDigit(sb.toString());
        sb.append(checkDigit);
        return sb.toString();
    }

    private int computeLuhnCheckDigit(String base) {
        int sum = 0;
        boolean doubleIt = true;

        for (int i = base.length() - 1; i >= 0; i--) {
            int digit = base.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return (10 - (sum % 10)) % 10;
    }

    public boolean isValidLuhn(String number) {
        if (number == null || number.length() != TOTAL_LENGTH) return false;
        int sum = 0;
        boolean doubleIt = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
