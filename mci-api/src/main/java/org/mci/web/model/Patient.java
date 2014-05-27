package org.mci.web.model;

public class Patient {

    private String healthId;

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Patient patient = (Patient) o;

        if (!healthId.equals(patient.healthId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return healthId.hashCode();
    }
}
