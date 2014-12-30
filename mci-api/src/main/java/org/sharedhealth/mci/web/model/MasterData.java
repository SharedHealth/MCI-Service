package org.sharedhealth.mci.web.model;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MasterData)) return false;

        MasterData that = (MasterData) o;

        if (pk != null ? !pk.equals(that.pk) : that.pk != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pk != null ? pk.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
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
