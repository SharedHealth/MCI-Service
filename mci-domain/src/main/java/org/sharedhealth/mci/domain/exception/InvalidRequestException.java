package org.sharedhealth.mci.domain.exception;

public class InvalidRequestException extends RuntimeException {
    private String errorMessage;


    public InvalidRequestException(String message) {
        super();
        this.errorMessage = message;
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }
}
