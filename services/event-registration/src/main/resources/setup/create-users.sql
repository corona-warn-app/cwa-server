/* Creates CWA users */

CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_event_flyway ENCRYPTED PASSWORD '<change me>';

/* --------------- Event Registration --------------- */
CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_event_creation ENCRYPTED PASSWORD '<change me>';
