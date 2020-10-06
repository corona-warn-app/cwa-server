

package app.coronawarn.server.services.federation.upload.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests which involve upload specific operations.
 */
@SpringBootTest
@ActiveProfiles({"integration-test"})
@DirtiesContext
@Tag("s3-integration")
abstract class UploadKeyIT {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setUpMocks() {
    // cleanup upload key table before tests run
    jdbcTemplate.execute("truncate table federation_upload_key");
  }
}
