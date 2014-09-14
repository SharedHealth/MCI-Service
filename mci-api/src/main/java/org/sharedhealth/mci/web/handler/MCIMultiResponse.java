package org.sharedhealth.mci.web.handler;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(value = { "httpStatusObject" })
@JsonPropertyOrder({"http_status","results","additional_info"})
public class MCIMultiResponse <T, A>{

    @JsonProperty("http_status")
    public int httpStatus;

    public List<T> results;

    @JsonProperty("additional_info")
    public List<A> additionalInfo;

    public HttpStatus httpStatusObject;

    public MCIMultiResponse(List<T> results, List<A> additionalInfo, HttpStatus httpStatusObject) {
        this.httpStatusObject = httpStatusObject;
        this.httpStatus = httpStatusObject.value();
        this.results = results;
        this.additionalInfo = additionalInfo;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public List<A> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(List<A> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public HttpStatus getHttpStatusObject() {
        return httpStatusObject;
    }

    public void setHttpStatusObject(HttpStatus httpStatusObject) {
        this.httpStatusObject = httpStatusObject;
    }
}
