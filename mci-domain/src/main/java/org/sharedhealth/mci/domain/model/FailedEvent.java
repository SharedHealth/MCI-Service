package org.sharedhealth.mci.domain.model;

import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.UUID;

import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.springframework.cassandra.core.PrimaryKeyType.CLUSTERED;
import static org.springframework.cassandra.core.PrimaryKeyType.PARTITIONED;

@Table(value = CF_FAILED_EVENTS)
public class FailedEvent {

    @PrimaryKeyColumn(name = FAILURE_TYPE, ordinal = 0, type = PARTITIONED)
    private String failureType;

    @PrimaryKeyColumn(name = EVENT_ID, ordinal = 1, type = CLUSTERED)
    private UUID eventId;

    @Column(ERROR_MESSAGE)
    private String errorMessage;

    @Column(FAILED_AT)
    private UUID failedAt;

    @Column(RETRIES)
    private int retries;

    public FailedEvent() {
    }

    public FailedEvent(String failureType, UUID eventId, String errorMessage) {
        this(failureType, eventId, errorMessage, 0);
    }

    public FailedEvent(String failureType, UUID eventId, String errorMessage, int retries) {
        this.failureType = failureType;
        this.eventId = eventId;
        this.errorMessage = errorMessage;
        this.retries = retries;
        this.failedAt = TimeUuidUtil.uuidForDate(new Date());
    }


    public UUID getEventId() {
        return eventId;
    }

    public int getRetries() {
        return retries;
    }
}
