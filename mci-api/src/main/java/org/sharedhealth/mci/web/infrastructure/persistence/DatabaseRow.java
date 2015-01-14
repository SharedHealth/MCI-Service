package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.Row;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

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

    public String getDateAsString(String name) {
        final Date value = this.getDate(name);
        return (value == null) ? null : DateFormatUtils.ISO_DATE_FORMAT.format(value);
    }
}
