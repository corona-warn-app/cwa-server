CREATE SCHEMA IF NOT EXISTS event_registration;

CREATE ROLE cwa_user
  NOLOGIN
  NOSUPERUSER
  NOINHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION;

GRANT CONNECT ON DATABASE cwa TO cwa_user;
GRANT USAGE ON SCHEMA event_registration TO cwa_user;

CREATE ROLE cwa_flyway
  NOLOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  IN ROLE cwa_user;

/* Flyway user needs to have full access to schema */
GRANT CREATE ON SCHEMA event_registration TO cwa_flyway;

 /* --------------- Event Registration --------------- */
CREATE ROLE cwa_event_registration
  NOLOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION
  IN ROLE cwa_user;
