package org.mci.web.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(value = NOT_FOUND)
    @ExceptionHandler(PatientNotFoundException.class)
    @ResponseBody
    public ErrorInfo handlePatientNotFoundException() {
        return new ErrorInfo(NOT_FOUND.value(), "patient.not.found");
    }

    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo handleException() {
        return new ErrorInfo(INTERNAL_SERVER_ERROR.value(), "internal.server.error");
    }

    private class ErrorInfo {
        @JsonProperty
        private int code;
        @JsonProperty
        private String message;

        private ErrorInfo(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
