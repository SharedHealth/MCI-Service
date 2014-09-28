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

CREATE INDEX location_district_id_key ON locations (district_id);
CREATE INDEX location_division_id_key ON locations (division_id);
CREATE INDEX location_pourashava_id_key ON locations (pourashava_id);
CREATE INDEX location_union_id_key ON locations (union_id);
CREATE INDEX location_upazilla_id_key ON locations (upazilla_id);
