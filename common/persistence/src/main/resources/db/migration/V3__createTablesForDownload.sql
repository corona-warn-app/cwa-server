CREATE TABLE federation_batch_info (
    batch_tag   varchar(20) PRIMARY KEY,
    date        date NOT NULL,
    status      varchar(20) NOT NULL DEFAULT 'UNPROCESSED'
);
