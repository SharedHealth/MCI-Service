package org.sharedhealth.mci.web.mapper;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sharedhealth.mci.validation.constraints.Length;
import org.sharedhealth.mci.validation.constraints.SearchQueryConstraint;


@JsonIgnoreProperties(ignoreUnknown = true)
@SearchQueryConstraint(message = "1006")
public class SearchQuery extends PaginationQuery {
    @JsonProperty("full_name")
    private String full_name;

    @JsonProperty("nid")
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = "1002")
    private String nid;

    @JsonProperty("bin_brn")
    @Pattern(regexp = "[\\d]{17}", message = "1002")
    private String bin_brn;

    @JsonProperty("uid")
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = "1002")
    private String uid;

    @JsonProperty("present_address")
    @Pattern(regexp = "[\\d]{6}|[\\d]{8}|[\\d]{10}", message = "1002")
    private String present_address;

    private String divisionId;
    private String districtId;
    private String upazilaId;

    @JsonProperty("sur_name")
    @Pattern(regexp = "^(\\s*)([A-Za-z0-9]{1,25})(\\b\\s*$)", message = "1002")
    private String sur_name;

    @JsonProperty("given_name")
    @Length(max = 100, min = 1, message = "1002")
    private String given_name;

    @JsonProperty("phone_no")
    @Pattern(regexp = "[0-9]{1,12}$", message = "1002")
    private String phone_no;

    @JsonProperty("country_code")
    @Pattern(regexp = "[0-9]*$", message = "1002")
    private String country_code;

    @JsonProperty("area_code")
    @Pattern(regexp = "[0-9]*$", message = "1002")
    private String area_code;

    @JsonProperty("extension")
    @Pattern(regexp = "[0-9]*$", message = "1002")
    private String extension;


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
        this.divisionId = present_address.substring(0, 2);
        this.districtId = present_address.substring(2, 4);
        this.upazilaId = present_address.substring(4, 6);
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "nid='" + nid + '\'' +
                ", bin_brn='" + bin_brn + '\'' +
                ", uid='" + uid + '\'' +
                ", present_address='" + present_address + '\'' +
                ", sur_name='" + sur_name + '\'' +
                ", given_name='" + given_name + '\'' +
                ", phone_no='" + phone_no + '\'' +
                ", country_code='" + country_code + '\'' +
                ", area_code='" + area_code + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }

    public String getDivisionId() {
        return divisionId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public String getUpazilaId() {
        return upazilaId;
    }

    public String getSur_name() {
        return sur_name;
    }

    public void setSur_name(String sur_name) {
        this.sur_name = sur_name;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getArea_code() {
        return area_code;
    }

    public void setArea_code(String area_code) {
        this.area_code = area_code;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
