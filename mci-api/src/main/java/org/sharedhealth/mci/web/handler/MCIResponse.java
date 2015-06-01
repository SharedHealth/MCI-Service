package org.sharedhealth.mci.web.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.utils.JsonConstants.HTTP_STATUS;

@JsonIgnoreProperties(value = {"httpStatusObject"})
@JsonPropertyOrder({HTTP_STATUS, "id"})
public class MCIResponse {

    @JsonProperty(HTTP_STATUS)
    public int httpStatus;

    @JsonInclude(NON_EMPTY)
    public String id;

    public HttpStatus httpStatusObject;

    public MCIResponse(HttpStatus httpStatusObject) {
        this.httpStatusObject = httpStatusObject;
        this.httpStatus = httpStatusObject.value();
    }

    public MCIResponse(String id, HttpStatus httpStatusObject) {
        this.id = id;
        this.httpStatusObject = httpStatusObject;
        this.httpStatus = httpStatusObject.value();
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
