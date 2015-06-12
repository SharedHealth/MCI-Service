package org.sharedhealth.mci.web.infrastructure.dedup.event;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DuplicatePatientUpdateEventProcessor extends DuplicatePatientEventProcessor {

    @Override
    public void process(String healthId, UUID marker) {

    }
}
