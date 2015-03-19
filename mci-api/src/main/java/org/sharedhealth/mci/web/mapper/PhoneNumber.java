package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.web.builder.DiffBuilder;
import org.sharedhealth.mci.web.builder.DiffResult;
import org.sharedhealth.mci.web.builder.Diffable;

import javax.validation.constraints.Pattern;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PhoneNumber implements Diffable<PhoneNumber> {

    @JsonProperty(COUNTRY_CODE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]*$", message = ERROR_CODE_PATTERN)
    private String countryCode;

    @JsonProperty(AREA_CODE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]*$", message = ERROR_CODE_PATTERN)
    private String areaCode;

    @JsonProperty(NUMBER)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]{1,12}$", message = ERROR_CODE_PATTERN)
    private String number;

    @JsonProperty(EXTENSION)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]*$", message = ERROR_CODE_PATTERN)
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

    @JsonIgnore
    public boolean isEmpty() {
        return this.equals(new PhoneNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;

        PhoneNumber that = (PhoneNumber) o;

        if (!defaultString(areaCode).equals(defaultString(that.areaCode))) return false;
        if (!defaultString(countryCode).equals(defaultString(that.countryCode))) return false;
        if (!defaultString(extension).equals(defaultString(that.extension))) return false;
        if (!defaultString(number).equals(defaultString(that.number))) return false;

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

    @Override
    public DiffResult diff(PhoneNumber that) {
        return new DiffBuilder(this, that)
                .append(COUNTRY_CODE, this.countryCode, that.countryCode)
                .append(AREA_CODE, this.areaCode, that.areaCode)
                .append(NUMBER, this.number, that.number)
                .append(EXTENSION, this.extension, that.extension)
                .build();
    }
}
