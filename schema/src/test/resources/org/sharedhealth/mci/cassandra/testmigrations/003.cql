CREATE TABLE IF NOT EXISTS brn_mapping (
    bin_brn varchar,
    health_id varchar,
    PRIMARY KEY (bin_brn, health_id)
);