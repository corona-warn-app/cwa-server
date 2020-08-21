CREATE TABLE federation_batch (
    batch_tag   varchar(20) PRIMARY KEY, --TODO validate length constraint
    date        date NOT NULL,
    status      varchar(20)
);
