package org.sharedhealth.mci.web.mapper;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.validation.constraints.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.utils.DateUtil.ISO_DATE_FORMAT;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.JsonConstants.DATE_OF_DEATH;
import static org.sharedhealth.mci.web.utils.JsonConstants.STATUS;
import static org.sharedhealth.mci.web.utils.JsonConstants.TYPE;

public class PatientStatus {

    @JsonProperty(TYPE)
    @JsonInclude(NON_EMPTY)
    @Code(type = STATUS, regexp = "[\\d]{1}", message = ERROR_CODE_INVALID)
    private String type;

    @JsonProperty(DATE_OF_DEATH)
    @JsonInclude(NON_EMPTY)
    @Date(format = ISO_DATE_FORMAT, message = ERROR_CODE_PATTERN)
    private String dateOfDeath;

    public String getDateOfDeath() {
        return this.dateOfDeath;
    }

    public void setDateOfDeath(String dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatientStatus that = (PatientStatus) o;

        if (dateOfDeath != null ? !dateOfDeath.equals(that.dateOfDeath) : that.dateOfDeath != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (dateOfDeath != null ? dateOfDeath.hashCode() : 0);
        return result;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.equals(new PatientStatus());
    }

    @Override
    public String toString() {
        return "PatientStatus{" +
                "type='" + type + '\'' +
                ", dateOfDeath='" + dateOfDeath + '\'' +
                '}';
    }
}
