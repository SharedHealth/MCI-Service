ALTER TABLE patient ADD lower_sur_name varchar;
ALTER TABLE patient ADD lower_given_name varchar;

CREATE INDEX lower_sur_name_key ON patient (lower_sur_name);
CREATE INDEX lower_given_name_key ON patient (lower_given_name);
