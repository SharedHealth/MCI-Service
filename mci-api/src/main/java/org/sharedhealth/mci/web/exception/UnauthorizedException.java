package org.sharedhealth.mci.web.exception;

public class UnauthorizedException extends RuntimeException {

    private String errorMessage;

    public UnauthorizedException(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
