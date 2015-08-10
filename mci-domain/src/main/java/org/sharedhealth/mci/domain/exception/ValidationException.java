package org.sharedhealth.mci.domain.exception;

import org.springframework.validation.BindingResult;

public class ValidationException extends RuntimeException {

    private BindingResult bindingResult;

    public ValidationException(BindingResult result) {
        super(result.toString());
        bindingResult = result;
    }

    public ValidationException(String message) {
        super(message);
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
