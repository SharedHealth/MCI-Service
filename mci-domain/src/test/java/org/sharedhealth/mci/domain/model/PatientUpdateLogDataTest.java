package org.sharedhealth.mci.domain.model;

import org.junit.Test;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.domain.repository.TestUtil.buildAddressChangeSet;

public class PatientUpdateLogDataTest {

    @Test
    public void shouldGetOldCatchmentFromChangeSet() throws Exception {
        PatientUpdateLog log = new PatientUpdateLog();
        log.setEventId(timeBased());
        log.setChangeSet(buildAddressChangeSet());

        PatientUpdateLogData logData = new PatientUpdateLogMapper().map(log);
        assertEquals(new Catchment("102030405060"), logData.getOldCatchmentFromChangeSet());
    }
}

