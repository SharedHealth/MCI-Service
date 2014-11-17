CREATE TABLE IF NOT EXISTS approval (
    health_id varchar,
    approval_fields map<text, text>,
    facility_id varchar,
    datetime timestamp,
    PRIMARY KEY (health_id, facility_id, datetime)
);