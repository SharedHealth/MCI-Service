package org.sharedhealth.mci.domain.exception;

public class InvalidRequesterException extends RuntimeException {
    private String errorMessage;


    public InvalidRequesterException(String message) {
        super();
        this.errorMessage = message;
    }

    public InvalidRequesterException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
