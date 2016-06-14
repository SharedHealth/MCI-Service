CREATE TABLE IF NOT EXISTS household_code_mapping (
    household_code varchar,
    health_id varchar,
    PRIMARY KEY (household_code, health_id)
);