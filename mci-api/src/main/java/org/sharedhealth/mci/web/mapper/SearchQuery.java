package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sharedhealth.mci.validation.constraints.Length;


@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQuery extends PaginationQuery {
    @JsonProperty("full_name")
    private String full_name;

    @JsonProperty("nid")
    private String nid;

    @JsonProperty("bin_brn")
    private String bin_brn;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("present_address")
    @Length(min = 6, max = 10, message = "1002")
    private String present_address;


    public SearchQuery() {
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getBin_brn() {
        return bin_brn;
    }

    public void setBin_brn(String bin_brn) {
        this.bin_brn = bin_brn;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPresent_address() {
        return present_address;
    }

    public void setPresent_address(String present_address) {
        this.present_address = present_address;
    }
}
