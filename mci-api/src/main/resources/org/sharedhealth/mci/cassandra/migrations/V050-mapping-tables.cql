CREATE TABLE IF NOT EXISTS nid_mapping (
    national_id varchar,
    health_id varchar,
    PRIMARY KEY (national_id, health_id)
);

CREATE TABLE IF NOT EXISTS brn_mapping (
    bin_brn varchar,
    health_id varchar,
    PRIMARY KEY (bin_brn, health_id)
);

CREATE TABLE IF NOT EXISTS uid_mapping (
    uid varchar,
    health_id varchar,
    PRIMARY KEY (uid, health_id)
);

CREATE TABLE IF NOT EXISTS phone_number_mapping (
    phone_no varchar,
    health_id varchar,
    PRIMARY KEY (phone_no, health_id)
);

CREATE TABLE IF NOT EXISTS name_mapping (
    division_id varchar,
    district_id varchar,
    upazilla_id varchar,
    given_name varchar,
    sur_name varchar,
    health_id varchar,
    PRIMARY KEY ((division_id, district_id, upazilla_id), given_name, sur_name, health_id)
);