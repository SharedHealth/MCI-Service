CREATE TABLE IF NOT EXISTS locations (
  geo_code text  PRIMARY KEY,
  district_id text,
  district_name text,
  division_id text,
  division_name text,
  pourashava_id text,
  pourashava_name text,
  union_id text,
  union_name text,
  upazilla_id text,
  upazilla_name text
);
