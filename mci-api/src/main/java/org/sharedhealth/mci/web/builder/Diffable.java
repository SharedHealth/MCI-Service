package org.sharedhealth.mci.web.builder;

public interface Diffable<T> {

    DiffResult diff(T obj);
}
