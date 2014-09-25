package org.sharedhealth.mci.web.exception;

import org.springframework.validation.BindingResult;

public class HealthIDExistException extends RuntimeException {

    private BindingResult bindingResult;

    public HealthIDExistException(BindingResult result) {
        super(result.toString());
        bindingResult = result;
    }

    public HealthIDExistException(String message) {
        super(message);
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
