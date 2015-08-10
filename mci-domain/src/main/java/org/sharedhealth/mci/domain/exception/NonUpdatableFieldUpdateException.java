package org.sharedhealth.mci.domain.exception;

public class NonUpdatableFieldUpdateException extends RuntimeException {

    public NonUpdatableFieldUpdateException(String message) {
        super(message);
    }
}
