package org.sharedhealth.mci.web.exception;

public class Unauthorized extends RuntimeException {

    private String errorMessage;

    public Unauthorized(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
