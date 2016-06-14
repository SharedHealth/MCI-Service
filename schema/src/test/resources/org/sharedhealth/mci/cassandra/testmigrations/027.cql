CREATE TABLE IF NOT EXISTS failed_events (
    failure_type varchar,
    event_id timeuuid,
    error_message varchar,
    failed_at timeuuid,
    retries int,
    PRIMARY KEY (failure_type, event_id)
) WITH CLUSTERING ORDER BY (event_id DESC);