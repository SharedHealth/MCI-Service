package org.sharedhealth.mci.web.mapper;

public class Catchment {

    private String divisionId;
    private String districtId;
    private String upazilaId;
    private String cityCorpId;
    private String unionOrUrbanWardId;
    private String ruralWardId;

    public Catchment(String divisionId, String districtId, String upazilaId) {
        this.divisionId = divisionId;
        this.districtId = districtId;
        this.upazilaId = upazilaId;
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

    public String getCityCorpId() {
        return cityCorpId;
    }

    public void setCityCorpId(String cityCorpId) {
        this.cityCorpId = cityCorpId;
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
}
