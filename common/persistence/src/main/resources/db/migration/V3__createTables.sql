CREATE TABLE federation_batch_download (
    batch_tag varchar(20) PRIMARY KEY, --TODO validate length constraint
    date      date NOT NULL
);
