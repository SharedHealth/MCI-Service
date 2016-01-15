DROP TABLE generated_hid_range;

CREATE TABLE IF NOT EXISTS generated_hid_range (
    block_begins_at bigint,
    begins_at bigint,
    ends_at bigint,
    allocated_for varchar,
    allocated_at timeuuid,
    requested_by varchar,
    PRIMARY KEY (block_begins_at, begins_at)
);