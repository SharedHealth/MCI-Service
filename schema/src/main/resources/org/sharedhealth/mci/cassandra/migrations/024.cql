CREATE TABLE patient_dupe (
    catchment_id varchar,
    health_id1 varchar,
    health_id2 varchar,
    reasons set<text>,
    created_at timeuuid,
    PRIMARY KEY (catchment_id, health_id1, health_id2));