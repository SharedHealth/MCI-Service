package org.mci.web.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Patient {

    @JsonIgnore
    private String healthId;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("present_address")
    private Address address;
    @JsonProperty("gender")
    private String gender;

    public String getHealthId() {
        return healthId;
    }

    public void setHealthId(String healthId) {
        this.healthId = healthId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Patient patient = (Patient) o;

        if (!address.equals(patient.address)) return false;
        if (!fullName.equals(patient.fullName)) return false;
        if (!gender.equals(patient.gender)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fullName.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + gender.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "healthId='" + healthId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", address=" + address +
                ", gender='" + gender + '\'' +
                '}';
    }
}
