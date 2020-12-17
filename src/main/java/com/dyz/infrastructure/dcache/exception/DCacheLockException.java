package com.dyz.infrastructure.dcache.exception;

public class DCacheLockException extends DCacheException {

    public DCacheLockException() {
        super();
    }

    public DCacheLockException(String message) {
        super(message);
    }

    public DCacheLockException(String message, Throwable cause) {
        super(message, cause);
    }
}

