CREATE TABLE IF NOT EXISTS catchment_mapping (
    catchment_id varchar,
    last_updated timeuuid,
    health_id varchar,
    PRIMARY KEY (catchment_id, last_updated, health_id)
)WITH CLUSTERING ORDER BY (last_updated ASC);