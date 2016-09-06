package org.sharedhealth.mci.web.model;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MciHealthIdStore {
    private Queue<String> mciHealthIds = new ConcurrentLinkedQueue<>();

    public synchronized void addMciHealthIds(Collection<String> mciHealthIds) {
        this.mciHealthIds.addAll(mciHealthIds);
    }

    public synchronized void clear() {
        this.mciHealthIds.clear();
    }

    public Collection<String> getAll() {
        return Collections.unmodifiableCollection(mciHealthIds);
    }

    public synchronized String getNextHealthId() {
        return this.mciHealthIds.remove();
    }

    public int noOfHIDsLeft() {
        return this.mciHealthIds.size();
    }
}
