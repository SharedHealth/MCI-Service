CREATE TABLE IF NOT EXISTS patient_audit_log (
    health_id varchar,
    event_id timeuuid,
    change_set varchar,
    requested_by varchar,
    approved_by varchar,
    PRIMARY KEY (health_id, event_id)
) WITH CLUSTERING ORDER BY (event_id ASC);