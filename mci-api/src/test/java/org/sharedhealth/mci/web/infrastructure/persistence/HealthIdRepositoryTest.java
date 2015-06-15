package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sharedhealth.mci.web.exception.HealthIdExhaustedException;
import org.sharedhealth.mci.web.model.HealthId;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class HealthIdRepositoryTest {
    @Mock
    CassandraOperations cqlTemplate;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(cqlTemplate.getConverter()).thenReturn(new MappingCassandraConverter());
    }

    @Test
    public void shouldSaveHidAsynchronously() {
        HealthIdRepository healthIdRepository = new HealthIdRepository(cqlTemplate);
        healthIdRepository.saveHealthId(new HealthId("98015440161", "MCI", 0));
        verify(cqlTemplate, times(1)).executeAsynchronously(any(Insert.class));
    }

    @Test
    public void shouldBlockIdsForMCIService() {
        ArrayList<HealthId> result = new ArrayList<>();
        result.add(new HealthId("898998"));
        result.add(new HealthId("898999"));
        when(cqlTemplate.select(any(Select.class), eq(HealthId.class))).thenReturn(result);
        HealthIdRepository healthIdRepository = new HealthIdRepository(cqlTemplate);

        List<HealthId> nextBlock = healthIdRepository.getNextBlock();

        ArgumentCaptor<Select> selectArgumentCaptor = ArgumentCaptor.forClass(Select.class);
        ArgumentCaptor<Class> classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
        assertEquals(2, nextBlock.size());
        verify(cqlTemplate, times(1)).select(selectArgumentCaptor.capture(), classArgumentCaptor.capture());
        assertFalse(selectArgumentCaptor.getValue().toString().contains("token"));
        assertEquals("898999", healthIdRepository.getLastReservedHealthId());

        healthIdRepository.getNextBlock();

        selectArgumentCaptor = ArgumentCaptor.forClass(Select.class);
        classArgumentCaptor = ArgumentCaptor.forClass(Class.class);
        assertEquals(2, nextBlock.size());
        verify(cqlTemplate, times(2)).select(selectArgumentCaptor.capture(), classArgumentCaptor.capture());
        assertTrue(selectArgumentCaptor.getValue().toString().contains("token"));
    }

    @Test(expected = HealthIdExhaustedException.class)
    public void exceptionResponseWhenIdsAReExhausted() {
        when(cqlTemplate.select(any(Select.class), eq(String.class))).thenReturn(new ArrayList<String>());
        HealthIdRepository healthIdRepository = new HealthIdRepository(cqlTemplate);
        healthIdRepository.getNextBlock();
    }
}