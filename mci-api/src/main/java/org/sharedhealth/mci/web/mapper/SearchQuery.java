package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.validation.constraints.Length;
import org.sharedhealth.mci.validation.constraints.SearchQueryConstraint;

import javax.validation.constraints.Pattern;

import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_PATTERN;
import static org.sharedhealth.mci.web.utils.ErrorConstants.ERROR_CODE_SEARCH_PARAMETER;


@JsonIgnoreProperties(ignoreUnknown = true)
@SearchQueryConstraint(message = ERROR_CODE_SEARCH_PARAMETER)
public class SearchQuery extends PaginationQuery {
    @JsonProperty("nid")
    @Pattern(regexp = "[\\d]{13}|[\\d]{17}", message = ERROR_CODE_PATTERN)
    private String nid;

    @JsonProperty("bin_brn")
    @Pattern(regexp = "[\\d]{17}", message = ERROR_CODE_PATTERN)
    private String bin_brn;

    @JsonProperty("uid")
    @Pattern(regexp = "[a-zA-Z0-9]{11}", message = ERROR_CODE_PATTERN)
    private String uid;

    @JsonProperty("present_address")
    private String present_address;

    private String divisionId;
    private String districtId;
    private String upazilaId;

    @JsonProperty("sur_name")
    @Pattern(regexp = "^(\\s*)([A-Za-z0-9]{1,25})(\\b\\s*$)", message = ERROR_CODE_PATTERN)
    private String sur_name;

    @JsonProperty("given_name")
    @Length(max = 100, min = 1, message = ERROR_CODE_PATTERN)
    private String given_name;

    @JsonProperty("phone_no")
    @Pattern(regexp = "[0-9]{1,12}$", message = ERROR_CODE_PATTERN)
    private String phone_no;

    @JsonProperty("country_code")
    @Pattern(regexp = "[0-9]*$", message = ERROR_CODE_PATTERN)
    private String country_code;

    @JsonProperty("area_code")
    @Pattern(regexp = "[0-9]*$", message = ERROR_CODE_PATTERN)
    private String area_code;

    @JsonProperty("extension")
    @Pattern(regexp = "[0-9]*$", message = ERROR_CODE_PATTERN)
    private String extension;

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
        if (present_address.length() > 1) this.divisionId = present_address.substring(0, 2);
        if (present_address.length() > 3) this.districtId = present_address.substring(2, 4);
        if (present_address.length() > 5) this.upazilaId = present_address.substring(4, 6);
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

    @JsonIgnore
    public boolean isEmpty() {

        if (StringUtils.isNotBlank(nid)) return false;
        if (StringUtils.isNotBlank(bin_brn)) return false;
        if (StringUtils.isNotBlank(uid)) return false;
        if (StringUtils.isNotBlank(present_address)) return false;
        if (StringUtils.isNotBlank(sur_name)) return false;
        if (StringUtils.isNotBlank(given_name)) return false;
        if (StringUtils.isNotBlank(phone_no)) return false;
        if (StringUtils.isNotBlank(country_code)) return false;
        if (StringUtils.isNotBlank(area_code)) return false;
        if (StringUtils.isNotBlank(extension)) return false;
        if (StringUtils.isNotBlank(divisionId)) return false;
        if (StringUtils.isNotBlank(districtId)) return false;
        if (StringUtils.isNotBlank(upazilaId)) return false;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchQuery)) return false;
        if (!super.equals(o)) return false;

        SearchQuery that = (SearchQuery) o;

        if (area_code != null ? !area_code.equals(that.area_code) : that.area_code != null) return false;
        if (bin_brn != null ? !bin_brn.equals(that.bin_brn) : that.bin_brn != null) return false;
        if (country_code != null ? !country_code.equals(that.country_code) : that.country_code != null) return false;
        if (districtId != null ? !districtId.equals(that.districtId) : that.districtId != null) return false;
        if (divisionId != null ? !divisionId.equals(that.divisionId) : that.divisionId != null) return false;
        if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
        if (given_name != null ? !given_name.equals(that.given_name) : that.given_name != null) return false;
        if (nid != null ? !nid.equals(that.nid) : that.nid != null) return false;
        if (phone_no != null ? !phone_no.equals(that.phone_no) : that.phone_no != null) return false;
        if (present_address != null ? !present_address.equals(that.present_address) : that.present_address != null)
            return false;
        if (sur_name != null ? !sur_name.equals(that.sur_name) : that.sur_name != null) return false;
        if (uid != null ? !uid.equals(that.uid) : that.uid != null) return false;
        if (upazilaId != null ? !upazilaId.equals(that.upazilaId) : that.upazilaId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nid != null ? nid.hashCode() : 0);
        result = 31 * result + (bin_brn != null ? bin_brn.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (present_address != null ? present_address.hashCode() : 0);
        result = 31 * result + (divisionId != null ? divisionId.hashCode() : 0);
        result = 31 * result + (districtId != null ? districtId.hashCode() : 0);
        result = 31 * result + (upazilaId != null ? upazilaId.hashCode() : 0);
        result = 31 * result + (sur_name != null ? sur_name.hashCode() : 0);
        result = 31 * result + (given_name != null ? given_name.hashCode() : 0);
        result = 31 * result + (phone_no != null ? phone_no.hashCode() : 0);
        result = 31 * result + (country_code != null ? country_code.hashCode() : 0);
        result = 31 * result + (area_code != null ? area_code.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        return result;
    }
}
