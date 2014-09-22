package org.sharedhealth.mci.web.handler;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.sharedhealth.mci.utils.FieldPropertyNameReader;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;


@JsonRootName(value = "error")
public class ErrorHandler {

    // MCI APPLICATION ERROR CODE
    public static final int VALIDATION_ERROR_CODE = 1000;
    public static final int INVALID_REQUEST_ERROR_CODE = 2000;
    public static final int PERMISSION_ERROR_CODE = 3000;


    @JsonProperty("error_code")
    private int errorCode;

    @JsonProperty("http_status")
    private int httpStatus;

    @JsonProperty
    private String message;

    @JsonProperty
    @JsonInclude(NON_EMPTY)
    private List<MCIError> errors;

    public ErrorHandler() {
    }

    public ErrorHandler(int httpStatus, int errorCode, String message) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.message = message;
    }

    public void addError(MCIError error) {

        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }

        this.errors.add(error);

    }

    public List<MCIError> getErrors() {
        return this.errors;
    }

    public String getMessage() {
        return this.message;
    }


    public Integer getHttpStatus() {
        return this.httpStatus;
    }

    public Integer getErrorCode() {
        return this.errorCode;
    }

    public ErrorHandler handleValidationError(ErrorHandler errorHandler, ValidationException e) {

        if (e.getBindingResult() != null) {
            for (ObjectError error : e.getBindingResult().getAllErrors()) {
                errorHandler.addError(getValidationErrorInfo(error));
            }
        }

        return errorHandler;
    }

    public ErrorHandler handleHttpMessageNotReadableError(ErrorHandler errorHandler, int code, String message) {

        MCIError mciError = new MCIError(code, message, "");
        errorHandler.addError(mciError);

        return errorHandler;
    }

    private MCIError getValidationErrorInfo(ObjectError error) {

        int code;
        String message,field;

        if (error.getDefaultMessage().matches("\\d+")) {
            code = Integer.parseInt(error.getDefaultMessage());
        } else {
            code = 0;
        }

        message = error.getDefaultMessage();
        field = getErrorField(error);

        if (field != "") {
            message = "invalid " + field;
        }

        return new MCIError(code, message, field);
    }


    private static String getErrorField(ObjectError error) {

        if (error.getClass() == FieldError.class) {
            FieldError fieldError = (FieldError) error;
            String field = fieldError.getField();

            return FieldPropertyNameReader.getFieldPropertyName(PatientMapper.class, field);
        }

        return "";
    }
}