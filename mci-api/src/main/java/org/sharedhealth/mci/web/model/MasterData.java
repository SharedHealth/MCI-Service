package org.sharedhealth.mci.web.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "master_data")
public class MasterData {

    @PrimaryKey
    private MasterDataKey pk;

    @Column("value")
    private String value;

    public MasterData(){}

    public MasterData(String type, String key, String value) {
        this.pk = new MasterDataKey(type, key);
        this.value = value;
    }

    @Override
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MasterDataKey getPk() {
        return pk;
    }

    public void setPk(MasterDataKey key) {
        this.pk = key;
    }

    public String getKey() {
        return this.pk.getKey();
    }

    public String getType() {
        return this.pk.getType();
    }
}
