CREATE TABLE IF NOT EXISTS master_data (
  type text,
  key text,
  value text,
  PRIMARY KEY (type, key)
);