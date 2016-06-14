CREATE TABLE IF NOT EXISTS org_healthid (
    health_id text,
    allocated_for varchar,
    generated_at timeuuid,
    is_used boolean,
    used_at timeuuid,
    PRIMARY KEY (health_id)
);