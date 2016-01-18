CREATE TABLE IF NOT EXISTS generated_hid_blocks (
    series_no bigint,
    allocated_for varchar,
    allocated_at timeuuid,
    begins_at bigint,
    ends_at bigint,
    total_hids bigint,
    requested_by varchar,
    PRIMARY KEY ((series_no), allocated_for, allocated_at)
);