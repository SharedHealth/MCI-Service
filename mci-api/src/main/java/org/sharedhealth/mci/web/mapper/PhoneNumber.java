package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;

        PhoneNumber that = (PhoneNumber) o;

        if (areaCode != null ? !areaCode.equals(that.areaCode) : that.areaCode != null) return false;
        if (countryCode != null ? !countryCode.equals(that.countryCode) : that.countryCode != null) return false;
        if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = countryCode != null ? countryCode.hashCode() : 0;
        result = 31 * result + (areaCode != null ? areaCode.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PhoneNumber{" +
                "countryCode='" + countryCode + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", number='" + number + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }
}
