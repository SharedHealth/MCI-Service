drop table if exists patient_duplicate;

CREATE TABLE IF NOT EXISTS patient_duplicate (
    catchment_id varchar,
    created_at timeuuid,
    health_id1 varchar,
    health_id2 varchar,
    reasons set<text>,
    PRIMARY KEY (catchment_id, created_at));

CREATE INDEX idx_patient_duplicate_health_id1 on patient_duplicate(health_id1);
