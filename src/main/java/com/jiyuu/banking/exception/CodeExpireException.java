package com.jiyuu.banking.exception;

public class CodeExpireException extends RuntimeException {
    public CodeExpireException(String message) { super(message); }
    public CodeExpireException(String message, Throwable cause) { super(message, cause); }
}
