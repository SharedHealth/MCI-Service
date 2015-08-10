package org.sharedhealth.mci.domain.diff;

public interface Diffable<T> {

    DiffResult diff(T obj);
}
