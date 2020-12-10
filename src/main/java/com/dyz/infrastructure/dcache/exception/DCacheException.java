package com.dyz.infrastructure.dcache.exception;

public class DCacheException extends RuntimeException {

    public DCacheException() {
        super();
    }

    public DCacheException(String message) {
        super(message);
    }

    public DCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}

