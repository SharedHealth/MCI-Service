package org.sharedhealth.mci.web.exception;

import org.springframework.validation.BindingResult;

public class HealthIdExistsException extends RuntimeException {

    private BindingResult bindingResult;

    public HealthIdExistsException(BindingResult result) {
        super(result.toString());
        bindingResult = result;
    }

    public HealthIdExistsException(String message) {
        super(message);
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
