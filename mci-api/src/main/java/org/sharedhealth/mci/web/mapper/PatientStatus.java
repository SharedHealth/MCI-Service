package org.sharedhealth.mci.web.mapper;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.sharedhealth.mci.utils.DateStringDeserializer;
import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.validation.constraints.Date;
import org.sharedhealth.mci.web.builder.DiffBuilder;
import org.sharedhealth.mci.web.builder.DiffResult;
import org.sharedhealth.mci.web.builder.Diffable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.JsonConstants.DATE_OF_DEATH;
import static org.sharedhealth.mci.web.utils.JsonConstants.STATUS;
import static org.sharedhealth.mci.web.utils.JsonConstants.TYPE;

public class PatientStatus implements Diffable<PatientStatus> {

    @JsonProperty(TYPE)
    @JsonInclude(NON_EMPTY)
    @Code(type = STATUS, regexp = "[\\d]{1}", message = ERROR_CODE_INVALID)
    private String type;

    @JsonProperty(DATE_OF_DEATH)
    @JsonInclude(NON_EMPTY)
    @Date(message = ERROR_CODE_PATTERN)
    @JsonDeserialize(using = DateStringDeserializer.class)
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

    @Override
    public DiffResult diff(PatientStatus that) {
        return new DiffBuilder(this, that)
                .append(TYPE, this.type, that.type)
                .append(DATE_OF_DEATH, this.dateOfDeath, that.dateOfDeath)
                .build();
    }


}
