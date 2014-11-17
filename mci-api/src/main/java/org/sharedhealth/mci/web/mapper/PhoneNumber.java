package org.sharedhealth.mci.web.mapper;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class PhoneNumber {

    @JsonProperty("country_code")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]*$", message = "1002")
    private String countryCode;

    @JsonProperty("area_code")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]*$", message = "1002")
    private String areaCode;

    @JsonProperty("number")
    @JsonInclude(NON_EMPTY)
    @NotNull(message = "1001", groups = RequiredOnUpdateGroup.class)
    @Pattern(regexp = "[0-9]{1,12}$", message = "1002")
    private String number;

    @JsonProperty("extension")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]*$", message = "1002")
    private String extension;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
