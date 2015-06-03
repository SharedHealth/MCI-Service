package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.data.cassandra.core.CassandraOperations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class HealthIdRepositoryTest {
    @Mock
    CassandraOperations cqlTemplate;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldSaveHidAsynchronously() {
        when(cqlTemplate.insertAsynchronously(any(HealthId.class))).thenReturn(HealthId.NULL_HID);
        HealthIdRepository healthIdRepository = new HealthIdRepository(cqlTemplate);
        healthIdRepository.saveHealthId(new HealthId("9801544016"));
        verify(cqlTemplate, times(1)).insertAsynchronously(any(HealthId.class));

    }
}