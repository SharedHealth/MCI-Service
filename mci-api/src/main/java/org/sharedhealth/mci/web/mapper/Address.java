package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.utils.PatientFieldProperties.*;

@JsonIgnoreProperties({"geoCode", "unionOrWard"})
public class Address {

    @JsonProperty(ADDRESS_LINE)
    @NotNull(message = "1001", groups = RequiredOnUpdateGroup.class)
    @Size(min = 3, max = 255, message = "1002")
    private String addressLine;

    @JsonProperty(DIVISION_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String divisionId;

    @JsonProperty(DISTRICT_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String districtId;

    @JsonProperty(UPAZILLA_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String upazillaId;

    @JsonProperty(UNION_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String unionId;

    @JsonProperty(HOLDING_NUMBER)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String holdingNumber;

    @JsonProperty(STREET)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String street;

    @JsonProperty(AREA_MOUJA)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String areaMouja;

    @JsonProperty(VILLAGE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String village;

    @JsonProperty(POST_OFFICE)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String postOffice;

    @JsonProperty(POST_CODE)
    @JsonInclude(NON_EMPTY)
    private String postCode;

    @JsonProperty(WARD_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String wardId;

    @JsonProperty(THANA_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String thanaId;

    @JsonProperty(CITY_CORPORATION_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String cityCorporationId;

    @JsonProperty(COUNTRY_CODE)
    @JsonInclude(NON_EMPTY)
    @Code(type = "country_code", regexp = "[\\d]{3}", message = "1004")
    private String countryCode = "050";

    @JsonIgnore
    @JsonProperty(UPAZILA_ID_OR_THANA_ID)
    private String upazilaOrThana;

    @JsonIgnore
    @JsonProperty(UPAZILLA_ID_AND_THANA_ID)
    private String upazilaAndThana;

    @JsonIgnore
    @JsonProperty(UNION_ID_AND_WARD_ID)
    private String unionAndWard;

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getUpazillaId() {
        return upazillaId;
    }

    public void setUpazillaId(String upazillaId) {
        this.upazillaId = upazillaId;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getHoldingNumber() {
        return holdingNumber;
    }

    public void setHoldingNumber(String holdingNumber) {
        this.holdingNumber = holdingNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAreaMouja() {
        return areaMouja;
    }

    public void setAreaMouja(String areaMouja) {
        this.areaMouja = areaMouja;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = postOffice;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCityCorporationId() {
        return cityCorporationId;
    }

    public void setCityCorporationId(String cityCorporationId) {
        this.cityCorporationId = cityCorporationId;
    }

    public String getThanaId() {
        return thanaId;
    }

    public void setThanaId(String thanaId) {
        this.thanaId = thanaId;
    }

    public String getWardId() {
        return wardId;
    }

    public void setWardId(String wardId) {
        this.wardId = wardId;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @JsonIgnore
    public String getUpazilaOrThana() {
        String ut = "";

        if (this.getUpazillaId() != null) {
            ut = this.getUpazillaId();
        }

        if (this.getThanaId() != null) {
            ut = ut + this.getThanaId();
        }

        return ut;
    }

    public String getUnionOrWard() {
        String uw = "";

        if (this.getUnionId() != null) {
            uw = this.getUnionId();
        }

        if (this.getWardId() != null) {
            uw = uw + this.getWardId();
        }

        return uw;
    }

    public String getGeoCode() {
        String gCode = "";

        if (StringUtils.isNotBlank(this.getDivisionId())) {
            gCode += this.getDivisionId();
        }

        if (StringUtils.isNotBlank(this.getDistrictId())) {
            gCode += this.getDistrictId();
        }

        if (StringUtils.isNotBlank(this.getUpazilaOrThana())) {
            gCode += this.getUpazilaOrThana();
        }

        if (StringUtils.isNotBlank(this.getCityCorporationId())) {
            gCode += this.getCityCorporationId();
        }

        if (StringUtils.isNotBlank(this.getUnionOrWard())) {
            gCode += this.getUnionOrWard();
        }

        return gCode;
    }

    @JsonIgnore
    public String getUpazilaAndThana() {
        return null;
    }

    @JsonIgnore
    public String getUnionAndWard() {
        return null;
    }
}
