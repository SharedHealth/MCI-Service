package org.sharedhealth.mci.web.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationData {

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonIgnore
    private String parent;

    @JsonIgnore
    private String divisionId;

    @JsonIgnore
    private String districtId;

    @JsonIgnore
    private String upazilaId;

    @JsonIgnore
    private String cityCorporationId;

    @JsonIgnore
    private String unionOrUrbanWardId;

    @JsonIgnore
    private String ruralWardId;

    @JsonIgnore
    private String geoCode;

    @JsonIgnore
    @JsonProperty("id")
    private String id;


    @JsonIgnore
    @JsonProperty("type")
    private String type;

    @JsonIgnore
    @JsonProperty("active")
    private String active;

    @JsonIgnore
    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonIgnore
    @JsonProperty("hierarchy")
    private LocationDataHierarchy hierarchy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationData)) return false;

        LocationData that = (LocationData) o;

        if (cityCorporationId != null ? !cityCorporationId.equals(that.cityCorporationId) : that.cityCorporationId != null)
            return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (districtId != null ? !districtId.equals(that.districtId) : that.districtId != null) return false;
        if (divisionId != null ? !divisionId.equals(that.divisionId) : that.divisionId != null) return false;
        if (geoCode != null ? !geoCode.equals(that.geoCode) : that.geoCode != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;
        if (ruralWardId != null ? !ruralWardId.equals(that.ruralWardId) : that.ruralWardId != null) return false;
        if (unionOrUrbanWardId != null ? !unionOrUrbanWardId.equals(that.unionOrUrbanWardId) : that.unionOrUrbanWardId != null)
            return false;
        if (upazilaId != null ? !upazilaId.equals(that.upazilaId) : that.upazilaId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (divisionId != null ? divisionId.hashCode() : 0);
        result = 31 * result + (districtId != null ? districtId.hashCode() : 0);
        result = 31 * result + (upazilaId != null ? upazilaId.hashCode() : 0);
        result = 31 * result + (cityCorporationId != null ? cityCorporationId.hashCode() : 0);
        result = 31 * result + (unionOrUrbanWardId != null ? unionOrUrbanWardId.hashCode() : 0);
        result = 31 * result + (ruralWardId != null ? ruralWardId.hashCode() : 0);
        result = 31 * result + (geoCode != null ? geoCode.hashCode() : 0);
        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
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

    public String getCityCorporationId() {
        return cityCorporationId;
    }

    public void setCityCorporationId(String cityCorporationId) {
        this.cityCorporationId = cityCorporationId;
    }

    public String getUnionOrUrbanWardId() {
        return unionOrUrbanWardId;
    }

    public void setUnionOrUrbanWardId(String unionOrUrbanWardId) {
        this.unionOrUrbanWardId = unionOrUrbanWardId;
    }

    public String getRuralWardId() {
        return ruralWardId;
    }

    public void setRuralWardId(String ruralWardId) {
        this.ruralWardId = ruralWardId;
    }

    public String getGeoCode() {
        return this.getCode() + this.getParent();
    }

    public void setGeoCode(String geoCode) {
        String code = geoCode.substring(geoCode.length() - 2, geoCode.length());
        String parent = geoCode.substring(0, geoCode.length() - 2);

        this.setCode(code);
        this.setParent(parent);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocationDataHierarchy getHierarchy() {
        return hierarchy;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class LocationDataHierarchy {

        @JsonProperty("code")
        private String code;

        @JsonProperty("name")
        private String name;

        @JsonProperty("type")
        private String type;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
