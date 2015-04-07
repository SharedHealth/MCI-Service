CREATE TABLE IF NOT EXISTS marker (
    type varchar,
    created_at timeuuid,
    marker varchar,
    PRIMARY KEY (type, created_at)
)  WITH CLUSTERING ORDER BY (created_at DESC);