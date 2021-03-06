package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Select;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.model.FailedEvent;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@RunWith(SpringJUnit4ClassRunner.class)
public class FailedEventsRepositoryIT extends BaseIntegrationTest {
    @Autowired
    private FailedEventsRepository failedEventsRepository;

    @Test
    public void shouldWriteToFailedEvents() throws Exception {
        String errorMessage = "message";
        UUID eventId = TimeUuidUtil.uuidForDate(new Date());
        failedEventsRepository.writeToFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, eventId, errorMessage);

        List<FailedEvent> failedEvents = getSearchMappingFailedEvents();
        assertThat(failedEvents.size(), is(1));
    }

    @Test
    public void shouldGetFailedEventsOfGivenTypeWithLimit() throws Exception {
        generateFailedEvents(50);

        List<FailedEvent> failedEvents = failedEventsRepository.getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, 1);
        assertThat(failedEvents.size(), is(1));

        failedEvents = failedEventsRepository.getFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, 100);
        assertThat(failedEvents.size(), is(50));

    }

    @Test
    public void shouldIncreaseRetriesByOneIfEventAlreadyExist() throws Exception {
        String errorMessage = "message";
        UUID eventId = TimeUuidUtil.uuidForDate(new Date());

        failedEventsRepository.writeToFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, eventId, errorMessage);

        List<FailedEvent> failedEvents = getSearchMappingFailedEvents();
        assertThat(failedEvents.size(), is(1));
        assertEquals(eventId, failedEvents.get(0).getEventId());
        assertEquals(0, failedEvents.get(0).getRetries());

        failedEventsRepository.writeToFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, eventId, errorMessage);

        failedEvents = getSearchMappingFailedEvents();
        assertThat(failedEvents.size(), is(1));
        assertEquals(eventId, failedEvents.get(0).getEventId());
        assertEquals(1, failedEvents.get(0).getRetries());

        failedEventsRepository.writeToFailedEvents(FAILURE_TYPE_SEARCH_MAPPING, eventId, errorMessage);

        failedEvents = getSearchMappingFailedEvents();
        assertThat(failedEvents.size(), is(1));
        assertEquals(eventId, failedEvents.get(0).getEventId());
        assertEquals(2, failedEvents.get(0).getRetries());
    }

    @Test
    public void shouldDeleteAFailedEvent() throws Exception {
        generateFailedEvents(20);
        FailedEvent failedEvent = getSearchMappingFailedEvents().get(0);

        failedEventsRepository.deleteFailedEvent(FAILURE_TYPE_SEARCH_MAPPING, failedEvent.getEventId());

        Select cqlFailedEventById = select().from(CF_FAILED_EVENTS)
                .where(eq(FAILURE_TYPE, FAILURE_TYPE_SEARCH_MAPPING))
                .and(eq(EVENT_ID, failedEvent.getEventId()))
                .limit(1);

        List<FailedEvent> failedEvents = cassandraOps.select(cqlFailedEventById, FailedEvent.class);
        assertTrue(isEmpty(failedEvents));
    }

    private List<FailedEvent> getSearchMappingFailedEvents() {
        String cqlFailedEvents = select().from(CF_FAILED_EVENTS).where(eq(FAILURE_TYPE, FAILURE_TYPE_SEARCH_MAPPING)).toString();
        return cassandraOps.select(cqlFailedEvents, FailedEvent.class);
    }

    private void generateFailedEvents(int limit) {
        Batch batch = batch();
        for (int i = 0; i < limit; i++) {
            String errorMessage = "message" + i;
            UUID eventId = TimeUuidUtil.uuidForDate(new Date());
            FailedEvent failedEvent = new FailedEvent(FAILURE_TYPE_SEARCH_MAPPING, eventId, errorMessage);
            batch.add(createInsertQuery(CF_FAILED_EVENTS, failedEvent, null, cassandraOps.getConverter()));
        }
        cassandraOps.execute(batch);
    }
}