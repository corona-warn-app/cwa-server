CREATE TABLE statistics_downloaded (
    counter SERIAL PRIMARY KEY,
    downloaded_timestamp bigint NOT NULL UNIQUE,
    etag varchar(256) NOT NULL
);

GRANT ALL ON TABLE statistics_downloaded TO "cwa_distribution";
GRANT USAGE, SELECT ON SEQUENCE statistics_downloaded_counter_seq TO "cwa_distribution";
