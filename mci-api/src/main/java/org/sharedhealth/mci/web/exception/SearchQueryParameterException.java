package org.sharedhealth.mci.web.exception;

import org.springframework.validation.BindingResult;

public class SearchQueryParameterException extends RuntimeException {

    private BindingResult bindingResult;

    public SearchQueryParameterException(BindingResult result) {
        super(result.toString());
        bindingResult = result;
    }

    public SearchQueryParameterException(String message) {
        super(message);
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
