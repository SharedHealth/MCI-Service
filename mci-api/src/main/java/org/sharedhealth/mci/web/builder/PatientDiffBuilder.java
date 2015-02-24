package org.sharedhealth.mci.web.builder;

import org.apache.commons.lang3.builder.Diff;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.mapper.Relation;

import java.util.*;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.sharedhealth.mci.web.utils.JsonConstants.*;

public class PatientDiffBuilder {

    static final String EMPTY_VALUE = "EMPTY";

    private final PatientData lhs;
    private final PatientData rhs;
    Map<String, Map<String, Object>> diffMap;

    public PatientDiffBuilder(PatientData lhs, PatientData rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Map<String, Map<String, Object>> build() {
        diffMap = new TreeMap<>();

        append(diff(lhs, rhs), null);
        append(diff(lhs.getPhoneNumber(), rhs.getPhoneNumber()), PHONE_NUMBER);
        append(diff(lhs.getPrimaryContactNumber(), rhs.getPrimaryContactNumber()), PRIMARY_CONTACT_NUMBER);
        append(diff(lhs.getAddress(), rhs.getAddress()), PRESENT_ADDRESS);
        append(diff(lhs.getPermanentAddress(), rhs.getPermanentAddress()), PERMANENT_ADDRESS);
        append(diff(lhs.getRelations(), rhs.getRelations()), null);

        return this.diffMap;
    }

    private void append(List<Diff<?>> diffs, String prefix) {
        prefix = isBlank(prefix) ? "" : prefix + ".";

        for (Diff<?> diff : diffs) {
            Map<String, Object> changeSet = new HashMap<>();
            changeSet.put(OLD_VALUE, defaultValue(diff.getKey()));
            changeSet.put(NEW_VALUE, defaultValue(diff.getValue()));
            diffMap.put(prefix + diff.getFieldName(), changeSet);
        }
    }

    private List<Diff<?>> diff(PatientData patient1, PatientData patient2) {
        if (patient1 == null) {
            patient1 = new PatientData();
        }
        if (patient2 == null) {
            patient2 = new PatientData();
        }
        return patient1.diff(patient2).getDiffs();
    }

    private List<Diff<?>> diff(PhoneNumber phoneNumber1, PhoneNumber phoneNumber2) {
        if (phoneNumber1 == null) {
            phoneNumber1 = new PhoneNumber();
        }
        if (phoneNumber2 == null) {
            phoneNumber2 = new PhoneNumber();
        }
        return phoneNumber1.diff(phoneNumber2).getDiffs();
    }

    private List<Diff<?>> diff(Address address1, Address address2) {
        if (address1 == null) {
            address1 = new Address();
        }
        if (address2 == null) {
            address2 = new Address();
        }
        return address1.diff(address2).getDiffs();
    }

    private List<Diff<?>> diff(final List<Relation> relations1, final List<Relation> relations2) {
        if (relations1 == null && relations2 == null) {
            return emptyList();
        }

        if (relations1 != null && relations2 != null
                && relations1.size() == relations2.size() && relations1.containsAll(relations2)) {
            return emptyList();
        }

        List<Diff<?>> diffs = new ArrayList<>();
        diffs.add(new Diff<Object>(RELATIONS) {
            @Override
            public Object getLeft() {
                return relations1;
            }

            @Override
            public Object getRight() {
                return relations2;
            }
        });
        return diffs;
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
