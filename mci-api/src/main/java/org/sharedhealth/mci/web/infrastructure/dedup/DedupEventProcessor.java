package org.sharedhealth.mci.web.infrastructure.dedup;

import java.util.UUID;

public interface DedupEventProcessor {

    void process(String healthId, UUID marker);
}
