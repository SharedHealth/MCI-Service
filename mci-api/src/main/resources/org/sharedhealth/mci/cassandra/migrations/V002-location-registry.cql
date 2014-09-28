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

CREATE INDEX district_id_key ON locations (district_id);
CREATE INDEX division_id_key ON locations (division_id);
CREATE INDEX pourashava_id_key ON locations (pourashava_id);
CREATE INDEX union_id_key ON locations (union_id);
CREATE INDEX upazilla_id_key ON locations (upazilla_id);
