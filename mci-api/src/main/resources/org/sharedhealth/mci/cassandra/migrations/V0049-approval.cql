CREATE TABLE IF NOT EXISTS approval_mapping (
    division_id varchar,
    district_id varchar,
    upazilla_id varchar,
    created_at timeuuid,
    health_id varchar,
    PRIMARY KEY ((division_id, district_id, upazilla_id), created_at)
) WITH CLUSTERING ORDER BY (created_at DESC);