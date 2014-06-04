package org.mci.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {

    @JsonProperty("address_line")
    private String addressLine;

    @JsonProperty("division_id")
    private String divisionId;

    @JsonProperty("district_id")
    private String districtId;

    @JsonProperty("upazilla_id")
    private String upazillaId;

    @JsonProperty("union_id")
    private String unionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (!addressLine.equals(address.addressLine)) return false;
        if (!districtId.equals(address.districtId)) return false;
        if (!divisionId.equals(address.divisionId)) return false;
        if (!unionId.equals(address.unionId)) return false;
        if (!upazillaId.equals(address.upazillaId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = addressLine.hashCode();
        result = 31 * result + divisionId.hashCode();
        result = 31 * result + districtId.hashCode();
        result = 31 * result + upazillaId.hashCode();
        result = 31 * result + unionId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Address{");
        sb.append("addressLine='").append(addressLine).append('\'');
        sb.append(", divisionId='").append(divisionId).append('\'');
        sb.append(", districtId='").append(districtId).append('\'');
        sb.append(", upazillaId='").append(upazillaId).append('\'');
        sb.append(", unionId='").append(unionId).append('\'');
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
}
