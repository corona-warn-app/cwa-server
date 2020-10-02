/* Creates CWA users */

CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_flyway ENCRYPTED PASSWORD '<change me>';
CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_submission ENCRYPTED PASSWORD '<change me>';
CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_distribution ENCRYPTED PASSWORD '<change me>';

/* --------------- Interoperability --------------- */
CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_federation_callback ENCRYPTED PASSWORD '<change me>';
CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_federation_download ENCRYPTED PASSWORD '<change me>';
CREATE USER "<change me>" WITH INHERIT IN ROLE cwa_federation_upload ENCRYPTED PASSWORD '<change me>';