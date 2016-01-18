CREATE TABLE IF NOT EXISTS org_healthid (
    health_id text,
    allocated_for varchar,
    is_available boolean,
    used_at timeuuid,
    PRIMARY KEY (health_id)
);