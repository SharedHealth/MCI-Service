package org.sharedhealth.mci.domain.model;


import org.sharedhealth.mci.domain.exception.InvalidRequestException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class RequesterDetails {

    private String id;
    private String name;

    public RequesterDetails() {
    }

    public RequesterDetails(String id) {
        this(id, null);
    }

    public RequesterDetails(String id, String name) {
        if (isBlank(id)) {
            throw new InvalidRequestException("Requester details id cannot be empty.");
        }
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequesterDetails)) return false;

        RequesterDetails that = (RequesterDetails) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequesterDetails{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
