CREATE TABLE IF NOT EXISTS locations (
    code text,
    name text,
    parent text,
    PRIMARY KEY (parent, code)
);
