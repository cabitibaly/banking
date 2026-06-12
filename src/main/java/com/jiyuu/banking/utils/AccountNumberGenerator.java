package com.jiyuu.banking.utils;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@NoArgsConstructor
public class AccountNumberGenerator {
    private static final String COUNTRY_CODE = "BF";
    private static final int BBAN_LENGTH = 23;

    public String generate() {
        String bban = generateBBAN();
        int checkDigits = computeCheckDigits(COUNTRY_CODE, bban);
        return COUNTRY_CODE + String.format("%02d", checkDigits) + bban;
    }

    private String generateBBAN() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(BBAN_LENGTH);
        for (int i = 0; i < BBAN_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private int computeCheckDigits(String countryCode, String bban) {
        String rearranged = bban + countryCode + "00";

        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                numeric.append(c);
            }
        }

        int remainder = mod97(numeric.toString());
        return 98 - remainder;
    }

    private int mod97(String numericString) {
        int remainder = 0;
        for (char c : numericString.toCharArray()) {
            remainder = (remainder * 10 + (c - '0')) % 97;
        }
        return remainder;
    }
}
