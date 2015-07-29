CREATE TABLE IF NOT EXISTS pending_approval_mapping (
    catchment_id varchar,
    last_updated timeuuid,
    health_id varchar,
    PRIMARY KEY (catchment_id, last_updated)
)WITH CLUSTERING ORDER BY (last_updated ASC);

CREATE INDEX IF NOT EXISTS idx_pending_approval_mapping_health_id on pending_approval_mapping(health_id);