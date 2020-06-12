/*
 * This SQL file needs to be executed when a new CWA database is set up.
 * It will create the necessary roles & restrict permissions to the CWA scope.
 */

/* Revoke all default access to the database */
REVOKE ALL ON DATABASE cwa FROM PUBLIC;
REVOKE USAGE ON SCHEMA public FROM PUBLIC;

/* Create roles */
CREATE ROLE cwa_user
  NOLOGIN
  NOSUPERUSER
  NOINHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION;

GRANT CONNECT ON DATABASE cwa TO cwa_user;
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
