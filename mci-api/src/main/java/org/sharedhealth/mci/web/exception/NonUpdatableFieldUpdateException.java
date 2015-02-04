package org.sharedhealth.mci.web.exception;

public class NonUpdatableFieldUpdateException extends RuntimeException {

    public NonUpdatableFieldUpdateException(String message) {
        super(message);
    }
}
