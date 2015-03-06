CREATE TABLE IF NOT EXISTS patient_update_log (
    year int,
    event_time timestamp,
    health_id varchar,
    change_set varchar,
    PRIMARY KEY (year, event_time, health_id)
) WITH CLUSTERING ORDER BY (event_time ASC);