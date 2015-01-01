package org.sharedhealth.mci.web.mapper;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Catchment {

    public static final String ERROR_DIVISION_ID_REQUIRED = "Division ID cannot be blank";
    private String divisionId;
    private String districtId;
    private String upazilaId;
    private String cityCorpId;
    private String unionOrUrbanWardId;
    private String ruralWardId;

    public Catchment(String divisionId) {
        if (isBlank(divisionId)) {
            throw new IllegalArgumentException(ERROR_DIVISION_ID_REQUIRED);
        }
        this.divisionId = divisionId;
    }

    public Catchment(String divisionId, String districtId, String upazilaId) {
        if (isBlank(divisionId)) {
            throw new IllegalArgumentException(ERROR_DIVISION_ID_REQUIRED);
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Catchment)) return false;

        Catchment catchment = (Catchment) o;

        if (cityCorpId != null ? !cityCorpId.equals(catchment.cityCorpId) : catchment.cityCorpId != null) return false;
        if (districtId != null ? !districtId.equals(catchment.districtId) : catchment.districtId != null) return false;
        if (!divisionId.equals(catchment.divisionId)) return false;
        if (ruralWardId != null ? !ruralWardId.equals(catchment.ruralWardId) : catchment.ruralWardId != null)
            return false;
        if (unionOrUrbanWardId != null ? !unionOrUrbanWardId.equals(catchment.unionOrUrbanWardId) : catchment.unionOrUrbanWardId != null)
            return false;
        if (upazilaId != null ? !upazilaId.equals(catchment.upazilaId) : catchment.upazilaId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = divisionId.hashCode();
        result = 31 * result + (districtId != null ? districtId.hashCode() : 0);
        result = 31 * result + (upazilaId != null ? upazilaId.hashCode() : 0);
        result = 31 * result + (cityCorpId != null ? cityCorpId.hashCode() : 0);
        result = 31 * result + (unionOrUrbanWardId != null ? unionOrUrbanWardId.hashCode() : 0);
        result = 31 * result + (ruralWardId != null ? ruralWardId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Catchment{");
        sb.append("divisionId='").append(divisionId).append('\'');
        sb.append(", districtId='").append(districtId).append('\'');
        sb.append(", upazilaId='").append(upazilaId).append('\'');
        sb.append(", cityCorpId='").append(cityCorpId).append('\'');
        sb.append(", unionOrUrbanWardId='").append(unionOrUrbanWardId).append('\'');
        sb.append(", ruralWardId='").append(ruralWardId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
