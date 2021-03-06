package org.sharedhealth.mci.web.handler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.domain.constant.JsonConstants.ADDITIONAL_INFO;
import static org.sharedhealth.mci.domain.constant.JsonConstants.HTTP_STATUS;

@JsonIgnoreProperties(value = {"httpStatusObject"})
@JsonPropertyOrder({HTTP_STATUS, "results", ADDITIONAL_INFO})
public class MCIMultiResponse {

    @JsonProperty(HTTP_STATUS)
    public int httpStatus;

    public Collection results;

    @JsonProperty(ADDITIONAL_INFO)
    @JsonInclude(NON_EMPTY)
    public HashMap<String, String> additionalInfo;

    public HttpStatus httpStatusObject;

    public MCIMultiResponse(Collection results, HashMap<String, String> additionalInfo, HttpStatus httpStatusObject) {
        this.httpStatusObject = httpStatusObject;
        this.httpStatus = httpStatusObject.value();
        this.results = results;
        this.additionalInfo = additionalInfo;
    }

    public MCIMultiResponse(HttpStatus httpStatusObject) {
        this.httpStatusObject = httpStatusObject;
        this.httpStatus = httpStatusObject.value();
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Collection getResults() {
        return results;
    }

    public void setResults(List<Object> results) {
        this.results = results;
    }

    public HashMap<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(HashMap<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public HttpStatus getHttpStatusObject() {
        return httpStatusObject;
    }

    public void setHttpStatusObject(HttpStatus httpStatusObject) {
        this.httpStatusObject = httpStatusObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MCIMultiResponse)) return false;

        MCIMultiResponse that = (MCIMultiResponse) o;

        if (httpStatus != that.httpStatus) return false;
        if (additionalInfo != null ? !additionalInfo.equals(that.additionalInfo) : that.additionalInfo != null)
            return false;
        if (httpStatusObject != that.httpStatusObject) return false;
        if (results != null ? !results.equals(that.results) : that.results != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = httpStatus;
        result = 31 * result + (results != null ? results.hashCode() : 0);
        result = 31 * result + (additionalInfo != null ? additionalInfo.hashCode() : 0);
        result = 31 * result + (httpStatusObject != null ? httpStatusObject.hashCode() : 0);
        return result;
    }
}
