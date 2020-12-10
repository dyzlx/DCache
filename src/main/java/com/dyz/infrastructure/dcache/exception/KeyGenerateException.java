package com.dyz.infrastructure.dcache.exception;

public class KeyGenerateException extends DCacheException {

    public KeyGenerateException() {
        super();
    }

    public KeyGenerateException(String message) {
        super(message);
    }

    public KeyGenerateException(String message, Throwable cause) {
        super(message, cause);
    }
}
