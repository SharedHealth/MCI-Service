CREATE TABLE IF NOT EXISTS locations (
    code text,
    name text,
    parent text,
    active varchar,
    PRIMARY KEY (parent, code)
);
