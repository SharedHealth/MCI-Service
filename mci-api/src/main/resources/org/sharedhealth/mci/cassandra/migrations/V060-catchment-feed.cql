ALTER TABLE patient DROP created_at;
ALTER TABLE patient ADD created_at timeuuid;

ALTER TABLE patient DROP updated_at;
ALTER TABLE patient ADD updated_at timeuuid;

DROP TABLE catchment_mapping;

CREATE TABLE catchment_mapping (
    catchment_id varchar,
    last_updated timeuuid,
    health_id varchar,
    PRIMARY KEY (catchment_id, last_updated, health_id)
)WITH CLUSTERING ORDER BY (last_updated ASC);
