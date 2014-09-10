
alter table patient add location_level1 varchar;
alter table patient add location_level2 varchar;
alter table patient add location_level3 varchar;
alter table patient add location_level4 varchar;
alter table patient add location_level5 varchar;
alter table patient add created_at timestamp;
alter table patient add updated_at timestamp;

CREATE INDEX location_level1_key ON patient (location_level1);
CREATE INDEX location_level2_key ON patient (location_level2);
CREATE INDEX location_level3_key ON patient (location_level3);
CREATE INDEX location_level4_key ON patient (location_level4);
CREATE INDEX location_level5_key ON patient (location_level5);