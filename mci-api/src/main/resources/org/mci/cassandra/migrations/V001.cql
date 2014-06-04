create table patient (
    health_id varchar PRIMARY KEY,
    national_id varchar,
    first_name varchar,
    middle_name varchar,
    last_name varchar,
    date_of_birth varchar,
    gender varchar,
    occupation varchar,
    edu_level varchar,
    primary_contact varchar,
    address_line varchar,
    division_id varchar,
    district_id varchar,
    upazilla_id varchar,
    union_id varchar);

CREATE INDEX national_id_key ON patient (national_id);