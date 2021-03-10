
REVOKE ALL ON DATABASE test FROM PUBLIC;
REVOKE USAGE ON SCHEMA public FROM PUBLIC;

/* Create roles */
CREATE ROLE cwa_user
  NOLOGIN
  NOSUPERUSER
  NOINHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION;

GRANT CONNECT ON DATABASE test TO cwa_user;
GRANT USAGE ON SCHEMA public TO cwa_user;

CREATE ROLE cwa_flyway
  NOLOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  IN ROLE cwa_user;

/* Flyway user needs to have full access to schema */
GRANT CREATE ON SCHEMA public TO cwa_flyway;

CREATE ROLE cwa_submission
  NOLOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  IN ROLE cwa_user;

CREATE ROLE cwa_distribution
  NOLOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  IN ROLE cwa_user;

/* --------------- Interoperability --------------- */
CREATE ROLE cwa_federation_callback
  NOLOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  IN ROLE cwa_user;

CREATE ROLE cwa_federation_download
   NOLOGIN
   NOSUPERUSER
   INHERIT
   NOCREATEDB
   NOCREATEROLE
   NOREPLICATION
   IN ROLE cwa_user;

 CREATE ROLE cwa_federation_upload
   NOLOGIN
   NOSUPERUSER
   INHERIT
   NOCREATEDB
   NOCREATEROLE
   NOREPLICATION
   IN ROLE cwa_user;

CREATE ROLE cwa_event_registration
  NOLOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  IN ROLE cwa_user;


CREATE USER "flyway" WITH INHERIT IN ROLE cwa_flyway ENCRYPTED PASSWORD '1234';
CREATE USER "local_submission" WITH INHERIT IN ROLE cwa_submission ENCRYPTED PASSWORD '1234';
CREATE USER "local_distribution" WITH INHERIT IN ROLE cwa_distribution ENCRYPTED PASSWORD '1234';

/* --------------- Interoperability --------------- */
CREATE USER "local_callback" WITH INHERIT IN ROLE cwa_federation_callback ENCRYPTED PASSWORD '1234';
CREATE USER "local_download" WITH INHERIT IN ROLE cwa_federation_download ENCRYPTED PASSWORD '1234';
CREATE USER "local_upload" WITH INHERIT IN ROLE cwa_federation_upload ENCRYPTED PASSWORD '1234';

/* --------------- Event Registration --------------- */
CREATE USER "local_event_registration" WITH INHERIT IN ROLE cwa_event_registration ENCRYPTED PASSWORD '1234';
