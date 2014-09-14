package org.sharedhealth.mci.web.handler;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.lang.String;

@JsonIgnoreProperties(value = { "httpStatusObject" })
@JsonPropertyOrder({"http_status","id"})
public class MCIResponse {

    @JsonProperty("http_status")
    public int httpStatus;

    public String id;

    public HttpStatus httpStatusObject;

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
