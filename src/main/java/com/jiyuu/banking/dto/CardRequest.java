package com.jiyuu.banking.dto;

import com.jiyuu.banking.enums.Network;

public record CardRequest(
        String accountNumber,
        Network network
) {
}
