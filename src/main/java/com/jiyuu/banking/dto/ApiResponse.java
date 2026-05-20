package com.jiyuu.banking.dto;

import java.time.Instant;

public record ApiResponse<T>(
        T data,
        String message,
        int status,
        Instant timestamp
) {
}
