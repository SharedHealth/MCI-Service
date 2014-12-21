CREATE TABLE IF NOT EXISTS pending_approval_mapping (
    division_id varchar,
    district_id varchar,
    upazila_id varchar,
    last_updated timeuuid,
    health_id varchar,
    PRIMARY KEY ((division_id, district_id, upazila_id), last_updated)
) WITH CLUSTERING ORDER BY (last_updated ASC);