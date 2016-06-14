CREATE TABLE IF NOT EXISTS name_mapping (
    division_id varchar,
    district_id varchar,
    upazila_id varchar,
    given_name varchar,
    sur_name varchar,
    health_id varchar,
    PRIMARY KEY ((division_id, district_id, upazila_id), given_name, sur_name, health_id)
);