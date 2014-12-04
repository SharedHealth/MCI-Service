package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Location {

    @JsonProperty("geo_code")
    private String geoCode;

    @JsonProperty("division_id")
    private String divisionId;

    @JsonProperty("division_name")
    private String divisionName;

    @JsonProperty("district_id")
    private String districtId;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("upazilla_id")
    private String upazilaId;

    @JsonProperty("upazilla_name")
    private String upazilaName;

    @JsonProperty("pourashava_id")
    private String paurashavaId;

    @JsonProperty("pourashava_name")
    private String paurashavaName;

    @JsonProperty("union_id")
    private String unionId;

    @JsonProperty("union_name")
    private String unionName;

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getGeoCode() {
        return geoCode;
    }

    public void setGeoCode(String geoCode) {
        this.geoCode = geoCode;
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

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getPaurashavaId() {
        return paurashavaId;
    }

    public void setPaurashavaId(String paurashavaId) {
        this.paurashavaId = paurashavaId;
    }

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getUpazilaName() {
        return upazilaName;
    }

    public void setUpazilaName(String upazilaName) {
        this.upazilaName = upazilaName;
    }

    public String getPaurashavaName() {
        return paurashavaName;
    }

    public void setPaurashavaName(String paurashavaName) {
        this.paurashavaName = paurashavaName;
    }

    public String getUnionName() {
        return unionName;
    }

    public void setUnionName(String unionName) {
        this.unionName = unionName;
    }
}
