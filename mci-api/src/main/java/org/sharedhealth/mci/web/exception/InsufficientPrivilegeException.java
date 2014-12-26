package org.sharedhealth.mci.web.exception;

public class InsufficientPrivilegeException extends RuntimeException {

    public InsufficientPrivilegeException(String message) {
        super(message);
    }

    public InsufficientPrivilegeException(String message, Throwable cause) {
        super(message, cause);
    }
}
