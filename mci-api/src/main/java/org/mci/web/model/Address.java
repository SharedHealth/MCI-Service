package org.mci.web.model;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.mci.web.annotation.AddressId;
import static org.mci.web.annotation.AddressType.DISTRICT;
import static org.mci.web.annotation.AddressType.DIVISION;
import static org.mci.web.annotation.AddressType.UNION;
import static org.mci.web.annotation.AddressType.UPAZILLA;

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
