package org.sharedhealth.mci.domain.model;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = "locations")
public class Location {

    @PrimaryKeyColumn(name = "code", ordinal = 0, type = PARTITIONED)
    private String code;

    @Column("name")
    private String name;

    @Column("active")
    private String active;

    @PrimaryKeyColumn(name = "parent", ordinal = 1, type = PARTITIONED)
    private String parent;

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

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Location() {
    }

    public Location(String code, String name, String parent, String active) {
        this.code = code;
        this.name = name;
        this.parent = parent;
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;

        Location location = (Location) o;

        if (code != null ? !code.equals(location.code) : location.code != null) return false;
        if (name != null ? !name.equals(location.name) : location.name != null) return false;
        if (parent != null ? !parent.equals(location.parent) : location.parent != null) return false;
        if (active != null ? !active.equals(location.active) : location.active != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        return result;
    }
}
