package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.domain.model.FailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class FailedEventsRepository extends BaseRepository {

    @Autowired
    public FailedEventsRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }


    public void writeToFailedEvents(String failureType, UUID eventId, String errorMessage) {
        List<FailedEvent> failedEvents = getEventIfAlreadyExist(failureType, eventId);
        if (isEmpty(failedEvents)) {
            FailedEvent failedEvent = new FailedEvent(failureType, eventId, errorMessage);
            cassandraOps.execute(createInsertQuery(CF_FAILED_EVENTS, failedEvent, null, cassandraOps.getConverter()));
        } else {
            int retries = failedEvents.get(0).getRetries();
            FailedEvent failedEvent = new FailedEvent(failureType, eventId, errorMessage, ++retries);
            cassandraOps.execute(createInsertQuery(CF_FAILED_EVENTS, failedEvent, null, cassandraOps.getConverter()));
        }
    }

    private List<FailedEvent> getEventIfAlreadyExist(String failureType, UUID eventId) {
        Select cqlFailedEvent = select().from(CF_FAILED_EVENTS).where(eq(FAILURE_TYPE, failureType)).and(eq(EVENT_ID, eventId)).limit(1);
        return cassandraOps.select(cqlFailedEvent, FailedEvent.class);
    }

    public List<FailedEvent> getFailedEvents(String failureType, int limit) {
        Select cqlFailedEvents = select().from(CF_FAILED_EVENTS).where(eq(FAILURE_TYPE, failureType)).limit(limit);
        return cassandraOps.select(cqlFailedEvents, FailedEvent.class);
    }

    public void deleteFailedEvent(String failureType, UUID eventId) {
        Delete deleteQuery = delete().from(CF_FAILED_EVENTS);
        deleteQuery.where(eq(FAILURE_TYPE, failureType)).and(eq(EVENT_ID, eventId));
        cassandraOps.execute(deleteQuery);
    }
}
