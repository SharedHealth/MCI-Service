package org.sharedhealth.mci.web.builder;

import org.apache.commons.lang3.builder.Diff;
import org.sharedhealth.mci.web.mapper.PatientData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.sharedhealth.mci.web.utils.JsonConstants.NEW_VALUE;
import static org.sharedhealth.mci.web.utils.JsonConstants.OLD_VALUE;

public class PatientDiffBuilder {

    static final String EMPTY_VALUE = "";

    private final PatientData lhs;
    private final PatientData rhs;

    public PatientDiffBuilder(PatientData lhs, PatientData rhs) {
        if (lhs == null) {
            throw new IllegalArgumentException("lhs cannot be null.");
        }
        if (rhs == null) {
            throw new IllegalArgumentException("rhs cannot be null.");
        }
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Map<String, Map<String, Object>> build() {
        Map<String, Map<String, Object>> diffMap = new TreeMap<>();
        List<Diff<?>> diffs = lhs.diff(rhs).getDiffs();
        for (Diff<?> diff : diffs) {
            Map<String, Object> changeSet = new HashMap<>();
            changeSet.put(OLD_VALUE, defaultValue(diff.getKey()));
            changeSet.put(NEW_VALUE, defaultValue(diff.getValue()));
            diffMap.put(diff.getFieldName(), changeSet);
        }
        return diffMap;
    }

    private Object defaultValue(Object o) {
        if (o == null) {
            return EMPTY_VALUE;
        }
        if (o instanceof String) {
            o = defaultString(valueOf(o));
        }
        return o;
    }
}
