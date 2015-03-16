package org.sharedhealth.mci.web.exception;

public class HidGenerationException extends RuntimeException {

    public HidGenerationException(String message) {
        super(message);
    }

    public HidGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
