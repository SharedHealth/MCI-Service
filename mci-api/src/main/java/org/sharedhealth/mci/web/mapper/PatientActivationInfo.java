package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sharedhealth.mci.validation.constraints.ExistingHid;
import org.sharedhealth.mci.web.builder.DiffBuilder;
import org.sharedhealth.mci.web.builder.DiffResult;
import org.sharedhealth.mci.web.builder.Diffable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.JsonConstants.ACTIVE;
import static org.sharedhealth.mci.web.utils.JsonConstants.MERGED_WITH;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientActivationInfo implements Diffable<PatientActivationInfo> {

    @JsonProperty(ACTIVE)
    @JsonInclude(NON_EMPTY)
    private Boolean active;

    @JsonProperty(MERGED_WITH)
    @JsonInclude(NON_EMPTY)
    @ExistingHid(message = ERROR_CODE_PATTERN)
    private String mergedWith;


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getMergedWith() {
        return mergedWith;
    }

    public void setMergedWith(String mergedWith) {
        this.mergedWith = mergedWith;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientActivationInfo)) return false;

        PatientActivationInfo that = (PatientActivationInfo) o;

        if (active != null ? !active.equals(that.active) : that.active != null) return false;
        if (mergedWith != null ? !mergedWith.equals(that.mergedWith) : that.mergedWith != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = active != null ? active.hashCode() : 0;
        result = 31 * result + (mergedWith != null ? mergedWith.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PatientActivationInfo{");
        sb.append("active=").append(active);
        sb.append(", mergedWith='").append(mergedWith).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public DiffResult diff(PatientActivationInfo obj) {
        return new DiffBuilder(this, obj)
                .append(ACTIVE, this.active, obj.active)
                .append(MERGED_WITH, this.mergedWith, obj.mergedWith)
                .build();
    }
}
