package com.darkhex.xeroims.exception;

public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException() {
        super();
    }

    public DuplicateProductException(String message) {
        super(message);
    }

    public DuplicateProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateProductException(Throwable cause) {
        super(cause);
    }
}
