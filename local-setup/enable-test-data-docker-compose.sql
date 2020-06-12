/* Testdata creation requires INSERT permission - this is only active on local deployments */
/* Since the Diagnosis Key Table does not yet exist, this is a workaround to still allow inserts */
ALTER DEFAULT PRIVILEGES FOR USER local_setup_flyway IN SCHEMA public GRANT INSERT ON TABLES TO cwa_distribution;
