package org.sharedhealth.mci.web.mapper;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sharedhealth.mci.validation.constraints.Code;
import org.sharedhealth.mci.validation.group.RequiredOnUpdateGroup;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

@JsonIgnoreProperties({"geoCode"})
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

    @JsonProperty(UPAZILA_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String upazilaId;

    @JsonProperty(CITY_CORPORATION_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String cityCorporationId;

    @JsonProperty(UNION_OR_URBAN_WARD_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String unionOrUrbanWardId;

    @JsonProperty(RURAL_WARD_ID)
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[\\d]{2}", message = "1002")
    private String ruralWardId;

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


    @JsonProperty(COUNTRY_CODE)
    @JsonInclude(NON_EMPTY)
    @Code(type = "country_code", regexp = "[\\d]{3}", message = "1004")
    private String countryCode = "050";

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

    public String getUpazilaId() {
        return upazilaId;
    }

    public void setUpazilaId(String upazilaId) {
        this.upazilaId = upazilaId;
    }

    public String getUnionOrUrbanWardId() {
        return unionOrUrbanWardId;
    }

    public void setUnionOrUrbanWardId(String unionOrUrbanWardId) {
        this.unionOrUrbanWardId = unionOrUrbanWardId;
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

    public String getRuralWardId() {
        return ruralWardId;
    }

    public void setRuralWardId(String ruralWardId) {
        this.ruralWardId = ruralWardId;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getGeoCode() {
        String gCode = "";

        if (StringUtils.isNotBlank(this.getDivisionId())) {
            gCode += this.getDivisionId();
        }

        if (StringUtils.isNotBlank(this.getDistrictId())) {
            gCode += this.getDistrictId();
        }

        if (StringUtils.isNotBlank(this.getUpazilaId())) {
            gCode += this.getUpazilaId();
        }

        if (StringUtils.isNotBlank(this.getCityCorporationId())) {
            gCode += this.getCityCorporationId();
        }

        if (StringUtils.isNotBlank(this.getUnionOrUrbanWardId())) {
            gCode += this.getUnionOrUrbanWardId();
        }

        if (StringUtils.isNotBlank(this.getRuralWardId())) {
            gCode += this.getRuralWardId();
        }

        return gCode;
    }
}
