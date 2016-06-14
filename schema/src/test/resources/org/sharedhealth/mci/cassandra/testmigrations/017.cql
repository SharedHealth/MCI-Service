CREATE TABLE IF NOT EXISTS patient_update_log (
    year int,
    event_id timeuuid,
    health_id varchar,
    change_set varchar,
    requested_by varchar,
    approved_by varchar,
    PRIMARY KEY (year, event_id, health_id)
) WITH CLUSTERING ORDER BY (event_id ASC);