package org.sharedhealth.mci.web.exception;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.sharedhealth.mci.web.handler.ErrorHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final int ERROR_CODE_JSON_PARSE = 125;
    public static final int ERROR_CODE_UNRECOGNIZED_FIELD = 126;

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ErrorHandler handleValidationException(ValidationException e) {

        logger.error("Handling ValidationException. ", e);
        ErrorHandler errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                ErrorHandler.VALIDATION_ERROR_CODE, "validation error");

        return errorHandler.handleValidationError(errorHandler, e);
    }

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ErrorHandler handleHttpMessageNotReadableExceptionException(HttpMessageNotReadableException e) {
        logger.error("Handling HttpMessageNotReadableExceptionException. ", e);

        int code = BAD_REQUEST.value();
        String msg = "invalid.request";
        Throwable cause = e.getCause();
        ErrorHandler errorHandler = null;
        if (cause != null && cause.getClass() == UnrecognizedPropertyException.class) {
            errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                    ErrorHandler.INVALID_REQUEST_ERROR_CODE, "invalid.request");
            code = ERROR_CODE_UNRECOGNIZED_FIELD;
            msg = "Unrecognized field: \"" + ((UnrecognizedPropertyException) cause).getPropertyName() + "\"";
        } else if (cause != null) {
            errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                    ErrorHandler.INVALID_REQUEST_ERROR_CODE, "invalid.request");
            code = ERROR_CODE_JSON_PARSE;
            msg = "invalid.json";
        }

        return errorHandler.handleHttpMessageNotReadableError(errorHandler, code, msg);
    }

    @ResponseStatus(value = NOT_FOUND)
    @ExceptionHandler(PatientNotFoundException.class)
    @ResponseBody
    public ErrorInfo handlePatientNotFoundException(PatientNotFoundException e) {
        logger.error("Handling PatientNotFoundException. ", e);
        return new ErrorInfo(NOT_FOUND.value(), "patient.not.found");
    }

    @ResponseStatus(value = CONFLICT)
    @ExceptionHandler(PatientAlreadyExistException.class)
    @ResponseBody
    public ErrorInfo handlePatientAlreadyExistException(PatientAlreadyExistException e) {
        logger.error("Handling PatientAlreadyExistException. ", e);
        return new ErrorInfo(CONFLICT.value(), "Patient already exist with health id: " + e.getMessage());
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo handleException(Exception e) {
        logger.error("Handling generic exception. ", e);
        return new ErrorInfo(INTERNAL_SERVER_ERROR.value(), "internal.server.error");
    }

    @JsonRootName(value = "error")
    public static class ErrorInfo implements Comparable<ErrorInfo> {


        @JsonProperty
        private String message;

        @JsonProperty
        private int code;


        @JsonProperty
        @JsonInclude(NON_EMPTY)
        private List<ErrorInfo> errors;

        public ErrorInfo() {
        }

        public ErrorInfo(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public void addError(ErrorInfo errorInfo) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }

            this.errors.add(errorInfo);
        }

        public List<ErrorInfo> getErrors() {
            return this.errors;
        }

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            return this.message;
        }

        @Override
        public int compareTo(ErrorInfo e) {
            return this.code > e.getCode() ? 1 : this.code < e.getCode() ? -1 : 0;
        }
    }
}
