package com.jiyuu.banking.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TypeOfPermission {
    ADMINSTRATOR_CREATE,
    ADMINSTRATOR_READ,
    ADMINSTRATOR_UPDATE,
    ADMINSTRATOR_DELETE,

    AGENT_CREATE,
    AGENT_READ,
    AGENT_UPDATE,
    AGENT_DELETE,

    CUSTOMER_CREATE,
    CUSTOMER_READ,
    CUSTOMER_UPDATE,
    CUSTOMER_DELETE;

    @Getter
    private String label;
}
