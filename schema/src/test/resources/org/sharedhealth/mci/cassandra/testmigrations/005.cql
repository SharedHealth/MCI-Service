CREATE TABLE IF NOT EXISTS phone_number_mapping (
    phone_no varchar,
    health_id varchar,
    PRIMARY KEY (phone_no, health_id)
);