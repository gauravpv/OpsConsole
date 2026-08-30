package com.opsconsole.tester.exception;

public class BajajTesterException extends RuntimeException {

    public BajajTesterException(String message) {
        super(message);
    }

    public BajajTesterException(String message, Throwable cause) {
        super(message, cause);
    }
}
