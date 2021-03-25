CREATE SCHEMA IF NOT EXISTS event_registration;

GRANT USAGE ON SCHEMA event_registration TO cwa_user;
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

