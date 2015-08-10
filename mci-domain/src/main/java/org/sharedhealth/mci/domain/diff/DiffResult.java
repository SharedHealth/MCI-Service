package org.sharedhealth.mci.domain.diff;

import org.apache.commons.lang3.builder.Diff;

import java.util.Iterator;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class DiffResult implements Iterable<Diff<?>> {

    private final List<Diff<?>> diffs;

    public DiffResult(final List<Diff<?>> diffs) {
        if (diffs == null) {
            throw new IllegalArgumentException("List of differences cannot be null");
        }
        this.diffs = diffs;
    }

    public List<Diff<?>> getDiffs() {
        return unmodifiableList(this.diffs);
    }

    @Override
    public Iterator<Diff<?>> iterator() {
        return this.diffs.iterator();
    }
}
