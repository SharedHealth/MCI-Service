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
import static org.sharedhealth.mci.web.utils.JsonConstants.ACTIVATED;
import static org.sharedhealth.mci.web.utils.JsonConstants.MERGED_WITH;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientActivationInfo implements Diffable<PatientActivationInfo> {

    @JsonProperty(ACTIVATED)
    @JsonInclude(NON_EMPTY)
    private Boolean activated;

    @JsonProperty(MERGED_WITH)
    @JsonInclude(NON_EMPTY)
    @ExistingHid(message = ERROR_CODE_PATTERN)
    private String mergedWith;


    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
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

        if (activated != null ? !activated.equals(that.activated) : that.activated != null) return false;
        if (mergedWith != null ? !mergedWith.equals(that.mergedWith) : that.mergedWith != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = activated != null ? activated.hashCode() : 0;
        result = 31 * result + (mergedWith != null ? mergedWith.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PatientActivationInfo{");
        sb.append("activated=").append(activated);
        sb.append(", mergedWith='").append(mergedWith).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public DiffResult diff(PatientActivationInfo obj) {
        return new DiffBuilder(this, obj)
                .append(ACTIVATED, this.activated, obj.activated)
                .append(MERGED_WITH, this.mergedWith, obj.mergedWith)
                .build();
    }
}
