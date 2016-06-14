CREATE TABLE IF NOT EXISTS patient_duplicate_ignored (
    health_id1 varchar,
    health_id2 varchar,
    reasons set<text>,
    PRIMARY KEY ((health_id1, health_id2))
);
