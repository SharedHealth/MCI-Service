package org.sharedhealth.mci.web.model;


import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.domain.model.Catchment;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

@Table(value = "facilities")
public class Facility {

    @PrimaryKey("id")
    private String id;

    @Column("name")
    private String name;

    @Column("catchments")
    private String catchments;

    @Column("type")
    private String type;

    @Column("location")
    private String location;

    public Facility(){}

    public Facility(String id, String name, String type, String catchments, String location) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.catchments = catchments;
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Catchment> getCatchmentsList() {

        List<String> catchmentsList = new ArrayList<>();

        if(StringUtils.isNotBlank(this.catchments)) {
            catchmentsList = Arrays.asList(this.catchments.split(","));
        }

        List<Catchment> catchments = new ArrayList<>();

        for (String catchment : catchmentsList) {
            catchments.add(new Catchment(catchment));
        }

        return catchments;
    }

    public void setCatchments(List<String> catchments) {
        this.catchments = join(catchments, ",");
    }

    public void setCatchmentsList(String catchments) {
        this.catchments = catchments;

    }

    public String getCatchments() {
        return catchments;
    }

    public void setCatchments(String catchments) {
        this.catchments = catchments;
        setCatchmentsList(catchments);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facility)) return false;

        Facility facility = (Facility) o;

        if (catchments != null ? !catchments.equals(facility.catchments) : facility.catchments != null) return false;
        if (id != null ? !id.equals(facility.id) : facility.id != null) return false;
        if (location != null ? !location.equals(facility.location) : facility.location != null) return false;
        if (name != null ? !name.equals(facility.name) : facility.name != null) return false;
        if (type != null ? !type.equals(facility.type) : facility.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (catchments != null ? catchments.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }
}
