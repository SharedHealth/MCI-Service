package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.web.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.mapper.ChangeSet;
import org.sharedhealth.mci.web.mapper.PatientAuditLogData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class PatientAuditRepositoryIT {

    @Autowired
    private PatientAuditRepository auditRepository;

    @Test
    public void shouldFindByHealthId() throws Exception {
        List<PatientAuditLogData> logs = auditRepository.findByHealthId("h100");
        assertNotNull(logs);
        assertEquals(3, logs.size());

        List<ChangeSet> changeSet1 = logs.get(0).getChangeSet();
        assertNotNull(changeSet1);
        assertEquals(3, changeSet1.size());
        assertEquals("given_name", changeSet1.get(0).getFieldName());
        assertEquals("edu_level", changeSet1.get(1).getFieldName());
        assertEquals("occupation", changeSet1.get(2).getFieldName());

        List<ChangeSet> changeSet2 = logs.get(1).getChangeSet();
        assertNotNull(changeSet2);
        assertEquals(1, changeSet2.size());

        List<ChangeSet> changeSet3 = logs.get(2).getChangeSet();
        assertNotNull(changeSet3);
        assertEquals(2, changeSet3.size());
    }
}