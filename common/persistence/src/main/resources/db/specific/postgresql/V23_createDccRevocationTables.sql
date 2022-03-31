CREATE TABLE revocation_entry (
    kid bytea NOT NULL,
    type bytea NOT NULL,
    hash bytea NOT NULL,
    x bytea NOT NULL,
    y bytea NOT NULL,
    PRIMARY KEY(kid, type, hash)
);

CREATE TABLE revocation_etag (
    path varchar(255) PRIMARY KEY,
    etag varchar(255) NOT NULL
);

GRANT ALL ON TABLE revocation_entry TO "cwa_distribution";
GRANT USAGE, SELECT ON SEQUENCE revocation_entry_id_seq TO "cwa_distribution";

GRANT SELECT,INSERT, DELETE ON TABLE revocation_etag TO "cwa_distribution";
GRANT USAGE, SELECT ON SEQUENCE revocation_etag_id_seq TO "cwa_distribution";
