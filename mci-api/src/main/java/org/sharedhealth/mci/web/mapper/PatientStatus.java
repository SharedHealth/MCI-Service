package org.sharedhealth.mci.web.mapper;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.sharedhealth.mci.utils.DateStringDeserializer;
import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.validation.constraints.Date;
import org.sharedhealth.mci.web.builder.DiffBuilder;
import org.sharedhealth.mci.web.builder.DiffResult;
import org.sharedhealth.mci.web.builder.Diffable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_INVALID;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
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

    public java.util.Date getDateOfDeath() {
        return this.dateOfDeath == null ? null : DateUtil.parseDate(this.dateOfDeath);
    }

    public void setDateOfDeath(java.util.Date dateOfDeath) {
        this.dateOfDeath = dateOfDeath == null ? null : DateUtil.toIsoMillisFormat(dateOfDeath);
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
        if (!(o instanceof PatientStatus)) return false;

        PatientStatus that = (PatientStatus) o;

        if (dateOfDeath == null) {
            if (that.dateOfDeath != null) return false;
        } else {
            if (!DateUtil.isEqualTo(getDateOfDeath(), that.getDateOfDeath())) return false;
        }
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
