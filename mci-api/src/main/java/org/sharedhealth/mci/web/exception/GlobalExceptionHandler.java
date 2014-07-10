package org.sharedhealth.mci.web.exception;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public GlobalExceptionHandler.ErrorInfo handleValidationException(ValidationException e) {
        logger.error("Handling ValidationException. ", e);
        ErrorInfo errorInfo = new ErrorInfo(BAD_REQUEST.value(), "invalid.request");

        for (ObjectError error: e.getBindingResult().getAllErrors()){
            errorInfo.addError(getValidationErrorInfo(error));
        }

        return errorInfo;
    }

    @ResponseStatus(value = NOT_FOUND)
    @ExceptionHandler(PatientNotFoundException.class)
    @ResponseBody
    public ErrorInfo handlePatientNotFoundException(PatientNotFoundException e) {
        logger.error("Handling PatientNotFoundException. ", e);
        return new ErrorInfo(NOT_FOUND.value(), "patient.not.found");
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo handleException(Exception e) {
        logger.error("Handling generic exception. ", e);
        return new ErrorInfo(INTERNAL_SERVER_ERROR.value(), "internal.server.error");
    }

    private ErrorInfo getValidationErrorInfo(ObjectError error) {
        int code;
        String msg;

        if (error.getDefaultMessage().matches("\\d+")) {
            code = Integer.parseInt(error.getDefaultMessage());
        } else {
            code = 0;
        }

        msg = getErrorMessage(error);

        return new ErrorInfo(code, msg);
    }

    private static String getErrorMessage(ObjectError error) {
        if(error.getClass() == FieldError.class) {
            return "Invalid " + ((FieldError) error).getField();
        }

        return "Invalid " + error.getDefaultMessage();
    }

    @JsonRootName(value = "error")
    public static class ErrorInfo {
        @JsonProperty
        private int code;
        @JsonProperty
        private String message;

        @JsonProperty
        @JsonInclude(NON_EMPTY)
        private List<ErrorInfo> errors;

        public ErrorInfo(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public void addError(ErrorInfo errorInfo) {
            if(this.errors == null) {
                this.errors = new ArrayList<ErrorInfo>();
            }

            this.errors.add(errorInfo);
        }
    }
}
