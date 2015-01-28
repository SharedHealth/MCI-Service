DROP INDEX location_level1_key;
DROP INDEX location_level2_key;
DROP INDEX location_level3_key;
DROP INDEX location_level4_key;
DROP INDEX location_level5_key;
DROP INDEX location_level6_key;

CREATE TABLE IF NOT EXISTS catchment_mapping (
    catchment_id varchar,
    last_updated timestamp,
    health_id varchar,
    PRIMARY KEY (catchment_id, last_updated, health_id)
)WITH CLUSTERING ORDER BY (last_updated ASC);
