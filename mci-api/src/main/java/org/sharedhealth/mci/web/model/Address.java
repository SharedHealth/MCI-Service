package org.sharedhealth.mci.web.model;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.sharedhealth.mci.validation.constraints.AddressId;
import static org.sharedhealth.mci.validation.AddressType.DISTRICT;
import static org.sharedhealth.mci.validation.AddressType.DIVISION;
import static org.sharedhealth.mci.validation.AddressType.UNION;
import static org.sharedhealth.mci.validation.AddressType.UPAZILLA;

public class Address {

    @JsonProperty("address_line")
    @NotBlank
    @Size(min = 3, max = 20)
    private String addressLine;

    @JsonProperty("division_id")
    @AddressId(DIVISION)
    private String divisionId;

    @JsonProperty("district_id")
    @AddressId(DISTRICT)
    private String districtId;

    @JsonProperty("upazilla_id")
    @AddressId(UPAZILLA)
    private String upazillaId;

    @JsonProperty("union_id")
    @AddressId(UNION)
    private String unionId;

    @JsonProperty("holding_number")
    private String holdingNumber;

    @JsonProperty("street")
    private String street;

    @JsonProperty("area_mouja")
    private String areaMouja;

    @JsonProperty("village")
    private String village;

    @JsonProperty("post_office")
    private String postOffice;

    @JsonProperty("post_code")
    private String postCode;

    @JsonProperty("ward")
    private String ward;

    @JsonProperty("thana")
    private String thana;

    @JsonProperty("city_corporation")
    private String cityCorporation;

    @JsonProperty("country")
    private String country;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (addressLine != null ? !addressLine.equals(address.addressLine) : address.addressLine != null) return false;
        if (districtId != null ? !districtId.equals(address.districtId) : address.districtId != null) return false;
        if (divisionId != null ? !divisionId.equals(address.divisionId) : address.divisionId != null) return false;
        if (unionId != null ? !unionId.equals(address.unionId) : address.unionId != null) return false;
        if (upazillaId != null ? !upazillaId.equals(address.upazillaId) : address.upazillaId != null) return false;

        return true;
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
        sb.append(", ward='").append(ward).append('\'');
        sb.append(", thana='").append(thana).append('\'');
        sb.append(", cityCorporation='").append(cityCorporation).append('\'');
        sb.append(", country='").append(country).append('\'');
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCityCorporation() {
        return cityCorporation;
    }

    public void setCityCorporation(String cityCorporation) {
        this.cityCorporation = cityCorporation;
    }

    public String getThana() {
        return thana;
    }

    public void setThana(String thana) {
        this.thana = thana;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
}
