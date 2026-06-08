package com.jiyuu.banking.enums;

import java.util.EnumSet;
import java.util.Set;

public enum CardState {
    PENDING,
    ACTIVE,
    BLOCKED,
    EXPIRED,
    CANCELED;

    private Set<CardState> allowedTransitions;

    static {
        PENDING.allowedTransitions = Set.of(ACTIVE);
        ACTIVE.allowedTransitions = Set.of(BLOCKED, EXPIRED, CANCELED);
        BLOCKED.allowedTransitions = Set.of(ACTIVE, EXPIRED, CANCELED);
        EXPIRED.allowedTransitions = Set.of();
        CANCELED.allowedTransitions = Set.of();
    }

    public boolean isAllowedTransition(CardState state) {
        return this.allowedTransitions.contains(state);
    }
}
