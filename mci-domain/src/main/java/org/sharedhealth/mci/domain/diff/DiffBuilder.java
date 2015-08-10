package org.sharedhealth.mci.domain.diff;

import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.Diff;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class DiffBuilder implements Builder<DiffResult> {

    private final List<Diff<?>> diffs;
    private final boolean objectsTriviallyEqual;

    public DiffBuilder(final Object lhs, final Object rhs) {
        if (lhs == null) {
            throw new IllegalArgumentException("lhs cannot be null");
        }

        if (rhs == null) {
            throw new IllegalArgumentException("rhs cannot be null");
        }

        this.diffs = new ArrayList<>();
        this.objectsTriviallyEqual = lhs == rhs || lhs.equals(rhs);
    }

    public DiffBuilder append(final String fieldName, final Object lhs, final Object rhs) {
        if (objectsTriviallyEqual) {
            return this;
        }
        if (rhs == null) {
            return this;
        }

        Class lhsClass = lhs != null ? lhs.getClass() : null;
        Class rhsClass = rhs.getClass();
        if (lhsClass != null && rhsClass != null && !lhsClass.equals(rhsClass)) {
            throw new IllegalArgumentException("Cannot compare objects of different types.");
        }

        if (rhs instanceof String) {
            String lhsString = defaultString((String) lhs);
            String rhsString = defaultString((String) rhs);
            if (lhsString.equals(rhsString)) {
                return this;
            }
        }

        if (rhs instanceof List) {
            List lhsList = (List) lhs;
            List rhsList = (List) rhs;
            if (lhsList != null && lhsList.size() == rhsList.size() && lhsList.containsAll(rhsList)) {
                return this;
            }
        }

        if (rhs.equals(lhs)) {
            return this;
        }

        diffs.add(new Diff<Object>(fieldName) {
            private static final long serialVersionUID = 1L;

            @Override
            public Object getLeft() {
                return lhs;
            }

            @Override
            public Object getRight() {
                return rhs;
            }
        });

        return this;
    }

    @Override
    public DiffResult build() {
        return new DiffResult(diffs);
    }
}
