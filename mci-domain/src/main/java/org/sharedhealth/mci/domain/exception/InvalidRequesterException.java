package org.sharedhealth.mci.domain.exception;

public class InvalidRequesterException extends RuntimeException {

    public InvalidRequesterException(String message) {
        super(message);
    }

    public InvalidRequesterException(String message, Throwable cause) {
        super(message, cause);
    }
}
