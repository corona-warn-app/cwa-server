package app.coronawarn.server.services.federation.upload.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base class for integration tests which involve upload specific operations.
 */
abstract class UploadKeyIT {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setUpMocks() {
    // cleanup upload key table before tests run
    jdbcTemplate.execute("truncate table federation_upload_key");
    jdbcTemplate.execute("truncate table chgs_upload_key");
  }
}
