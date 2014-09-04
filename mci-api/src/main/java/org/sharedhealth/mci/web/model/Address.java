package org.sharedhealth.mci.web.model;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.validation.constraints.AddressId;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.sharedhealth.mci.validation.AddressType.*;

@JsonIgnoreProperties({ "geoCode", "upazilaOrThana", "unionOrWard" })
public class Address {

    @JsonProperty("address_line")
    @NotBlank(message = "1001")
    @Size(min = 3, max = 20, message = "1002")
    private String addressLine;

    @JsonProperty("division_id")
    @JsonInclude(NON_EMPTY)
    @AddressId(value = DIVISION, message = "1004")
    private String divisionId;

    @JsonProperty("district_id")
    @JsonInclude(NON_EMPTY)
    @AddressId(value = DISTRICT, message = "1004")
    private String districtId;

    @JsonProperty("upazilla_id")
    @JsonInclude(NON_EMPTY)
    @AddressId(value = UPAZILLA, message = "1004")
    private String upazillaId;

    @JsonProperty("union_id")
    @JsonInclude(NON_EMPTY)
    @AddressId(value = UNION, message = "1004")
    private String unionId;

    @JsonProperty("holding_number")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String holdingNumber;

    @JsonProperty("street")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String street;

    @JsonProperty("area_mouja")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String areaMouja;

    @JsonProperty("village")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String village;

    @JsonProperty("post_office")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1002")
    private String postOffice;

    @JsonProperty("post_code")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "[0-9]{4}$", message = "1004")
    private String postCode;

    @JsonProperty("ward_id")
    @JsonInclude(NON_EMPTY)
    @AddressId(value = WARD, message = "1004")
    private String wardId;

    @JsonProperty("thana_id")
    @JsonInclude(NON_EMPTY)
    @Pattern(regexp = "^[\\s\\S]{0,50}$", message = "1004")
    private String thanaId;

    @JsonProperty("city_corporation_id")
    @JsonInclude(NON_EMPTY)
    @AddressId(value = CITYCORPORATION, message = "1004")
    private String cityCorporationId;

    @JsonProperty("country_code")
    @JsonInclude(NON_EMPTY)
    @AddressId(value=COUNTRY, message = "1004")
    private String countryCode;

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
        final StringBuilder sb = new StringBuilder("Address{");
        sb.append("addressLine='").append(addressLine).append('\'');
        sb.append(", divisionId='").append(divisionId).append('\'');
        sb.append(", districtId='").append(districtId).append('\'');
        sb.append(", upazillaId='").append(upazillaId).append('\'');
        sb.append(", unionId='").append(unionId).append('\'');
        sb.append(", holdingNumber='").append(holdingNumber).append('\'');
        sb.append(", street='").append(street).append('\'');
        sb.append(", areaMouja='").append(areaMouja).append('\'');
        sb.append(", village='").append(village).append('\'');
        sb.append(", postOffice='").append(postOffice).append('\'');
        sb.append(", postCode='").append(postCode).append('\'');
        sb.append(", wardId='").append(wardId).append('\'');
        sb.append(", thanaId='").append(thanaId).append('\'');
        sb.append(", cityCorporationId='").append(cityCorporationId).append('\'');
        sb.append(", countryCode='").append(countryCode).append('\'');
        sb.append('}');
        return sb.toString();
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

    public  String getUpazilaOrThana() {
        String ut = "";

        if(this.getUpazillaId() != null) {
            ut = this.getUpazillaId();
        }

        if(this.getThanaId() != null) {
            ut = ut + this.getThanaId();
        }

        return ut;
    }

    public String getUnionOrWard() {
        String uw = "";

        if(this.getUnionId() != null) {
            uw = this.getUnionId();
        }

        if(this.getWardId() != null) {
            uw = uw + this.getWardId();
        }

        return uw;
    }

    public String getGeoCode() {

        final StringBuilder sb = new StringBuilder("");

        sb.append(this.getDivisionId());
        sb.append(this.getDistrictId());
        sb.append(this.getUpazilaOrThana());
        sb.append(this.getCityCorporationId());
        sb.append(this.getUnionOrWard());

        return sb.toString();
    }
}
