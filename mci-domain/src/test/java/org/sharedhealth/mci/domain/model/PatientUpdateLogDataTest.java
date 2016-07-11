package org.sharedhealth.mci.domain.model;

import org.junit.Test;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.sharedhealth.mci.domain.util.TestUtil.buildAddressChangeSet;

public class PatientUpdateLogDataTest {

    @Test
    public void shouldGetOldCatchmentFromChangeSet() throws Exception {
        PatientUpdateLog log = new PatientUpdateLog();
        log.setEventId(TimeUuidUtil.uuidForDate(new Date()));
        log.setChangeSet(buildAddressChangeSet());

        PatientUpdateLogData logData = new PatientUpdateLogMapper().map(log);
        assertEquals(new Catchment("102030405060"), logData.getOldCatchmentFromChangeSet());
    }
}

