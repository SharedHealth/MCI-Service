CREATE TABLE IF NOT EXISTS uid_mapping (
    uid varchar,
    health_id varchar,
    PRIMARY KEY (uid, health_id)
);