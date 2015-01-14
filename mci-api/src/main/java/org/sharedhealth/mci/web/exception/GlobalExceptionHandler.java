package org.sharedhealth.mci.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.sharedhealth.mci.web.handler.ErrorHandler;
import org.sharedhealth.mci.web.utils.JsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final int ERROR_CODE_JSON_PARSE = 2001;
    public static final int ERROR_CODE_UNRECOGNIZED_FIELD = 2002;
    public static final int ERROR_CODE_FIELD_NOT_PERMITTED = 3001;
    private static final String MESSAGE_VALIDATION_ERROR = "validation error";
    private static final String MESSAGE_INVALID_REQUEST = "invalid.request";
    private static final String MESSAGE_INVALID_JSON = "invalid.json";
    private static final String MESSAGE_UNRECOGNIZED_FIELD = "Unrecognized field: '%s'";
    private static final String MESSAGE_PATIENT_NOT_FOUND = "patient.not.found";
    private static final String MESSAGE_FACILITY_NOT_FOUND = "facility.not.found";
    private static final String MESSAGE_PATIENT_ALREADY_EXIST_WITH_HEALTH_ID = "Patient already exist with health id: %s";
    private static final String MESSAGE_PERMISSION_ERROR = "permission.error";
    private static final String MESSAGE_HID_FIELD_IS_NOT_PERMITTED = "hid field is not permitted";
    private static final String MESSAGE_INTERNAL_SERVER_ERROR = "internal.server.error";

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ErrorHandler handleValidationException(ValidationException e) {

        logger.error("Handling ValidationException. ", e);
        ErrorHandler errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                ErrorHandler.VALIDATION_ERROR_CODE, MESSAGE_VALIDATION_ERROR);

        return errorHandler.handleValidationError(errorHandler, e);
    }

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ErrorHandler handleHttpMessageNotReadableExceptionException(HttpMessageNotReadableException e) {
        logger.error("Handling HttpMessageNotReadableExceptionException. ", e);

        int code = BAD_REQUEST.value();
        String msg = MESSAGE_INVALID_REQUEST;
        String field = null;
        Throwable cause = e.getCause();
        ErrorHandler errorHandler = null;
        if (cause != null && cause.getClass() == UnrecognizedPropertyException.class) {
            errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                    ErrorHandler.INVALID_REQUEST_ERROR_CODE, MESSAGE_INVALID_REQUEST);
            code = ERROR_CODE_UNRECOGNIZED_FIELD;
            field = ((UnrecognizedPropertyException) cause).getPropertyName();
            msg = String.format(MESSAGE_UNRECOGNIZED_FIELD, field);
        } else if (cause != null) {
            errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                    ErrorHandler.INVALID_REQUEST_ERROR_CODE, MESSAGE_INVALID_REQUEST);
            code = ERROR_CODE_JSON_PARSE;
            msg = MESSAGE_INVALID_JSON;
        }

        return errorHandler != null ? errorHandler.handleHttpMessageNotReadableError(errorHandler, code, msg, field) : null;
    }

    @ResponseStatus(value = NOT_FOUND)
    @ExceptionHandler(PatientNotFoundException.class)
    @ResponseBody
    public ErrorHandler handlePatientNotFoundException(PatientNotFoundException e) {
        logger.error("Handling PatientNotFoundException. ", e);
        return new ErrorHandler(NOT_FOUND.value(), MESSAGE_PATIENT_NOT_FOUND);
    }

    @ResponseStatus(value = NOT_FOUND)
    @ExceptionHandler(FacilityNotFoundException.class)
    @ResponseBody
    public ErrorHandler handleFacilityNotFoundException(FacilityNotFoundException e) {
        logger.error("Handling FacilityNotFoundException. ", e);

        return new ErrorHandler(NOT_FOUND.value(), MESSAGE_FACILITY_NOT_FOUND);
    }

    @ResponseStatus(value = CONFLICT)
    @ExceptionHandler(PatientAlreadyExistException.class)
    @ResponseBody
    public ErrorInfo handlePatientAlreadyExistException(PatientAlreadyExistException e) {
        logger.error("Handling PatientAlreadyExistException. ", e);
        return new ErrorInfo(CONFLICT.value(), String.format(MESSAGE_PATIENT_ALREADY_EXIST_WITH_HEALTH_ID, e.getMessage()));
    }

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(HealthIDExistException.class)
    @ResponseBody
    public ErrorHandler healthIDExistException(HealthIDExistException e) {
        logger.error("Handling Health ID exist exception. ", e);
        int code;
        String msg, field;
        ErrorHandler errorHandler;

        errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                ErrorHandler.PERMISSION_ERROR_CODE, MESSAGE_PERMISSION_ERROR);
        code = ERROR_CODE_FIELD_NOT_PERMITTED;
        msg = MESSAGE_HID_FIELD_IS_NOT_PERMITTED;
        field = JsonConstants.HID;

        return errorHandler.handleHealthIDExistError(errorHandler, code, msg, field);
    }

    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(SearchQueryParameterException.class)
    @ResponseBody
    public ErrorHandler searchQueryParameterException(SearchQueryParameterException e) {
        logger.error("Handling Search Query parameter exception. ", e);

        ErrorHandler errorHandler;

        errorHandler = new ErrorHandler(BAD_REQUEST.value(),
                ErrorHandler.VALIDATION_ERROR_CODE, MESSAGE_VALIDATION_ERROR);

        return errorHandler.handleSearchQueryParameterError(e);
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo handleException(Exception e) {
        logger.error("Handling generic exception. ", e);
        return new ErrorInfo(INTERNAL_SERVER_ERROR.value(), MESSAGE_INTERNAL_SERVER_ERROR);
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
            if (this.code < e.getCode()) return this.code > e.getCode() ? 1 : -1;
            else return this.code > e.getCode() ? 1 : 0;
        }
    }
}
