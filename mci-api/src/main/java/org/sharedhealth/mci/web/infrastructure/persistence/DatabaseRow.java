package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.Date;

import com.datastax.driver.core.Row;
import org.apache.commons.lang3.StringUtils;

public class DatabaseRow {

    private final Row row;

    public DatabaseRow(Row row) {
        this.row = row;
    }

    public String getString(String name) {
        final String value = row.getString(name);
        return StringUtils.isBlank(value) || "null".equals(value) ? null : value;
    }

    public Date getDate(String name) {
        final Date value = row.getDate(name);
        return "null".equals(value) ? null : value;
    }
}
