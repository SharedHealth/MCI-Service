CREATE TABLE IF NOT EXISTS generated_hid_blocks (
    series_no bigint,
    generated_for varchar,
    generated_at timeuuid,
    begins_at bigint,
    ends_at bigint,
    total_hids bigint,
    requested_by varchar,
    PRIMARY KEY ((series_no), generated_for, generated_at)
);