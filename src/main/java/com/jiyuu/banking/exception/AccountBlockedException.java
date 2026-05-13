package com.jiyuu.banking.exception;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(String message) { super(message); }
    public AccountBlockedException(String message, Throwable cause) { super(message, cause); }
}
