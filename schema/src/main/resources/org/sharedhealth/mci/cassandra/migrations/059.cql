DROP TABLE patient_update_log;

CREATE TABLE patient_update_log (
    year int,
    event_id timeuuid,
    health_id varchar,
    change_set varchar,
    PRIMARY KEY (year, event_id, health_id)
) WITH CLUSTERING ORDER BY (event_id ASC);