CREATE TABLE IF NOT EXISTS nid_mapping (
    national_id varchar,
    health_id varchar,
    PRIMARY KEY (national_id, health_id)
);