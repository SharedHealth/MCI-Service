package org.sharedhealth.mci.web.mapper;

import org.junit.Test;
import org.sharedhealth.mci.web.model.PatientUpdateLog;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.web.infrastructure.persistence.TestUtil.buildAddressChangeSet;

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

