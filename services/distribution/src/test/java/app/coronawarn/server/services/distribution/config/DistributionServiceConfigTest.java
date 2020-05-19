package app.coronawarn.server.services.distribution.config;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@TestPropertySource("classpath:application.properties")
public class DistributionServiceConfigTest {

  @Autowired
  private DistributionServiceConfig config;

  private final Properties properties = new Properties();

  @BeforeEach
  public void setup() throws IOException {
    InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties");
    properties.load(is);
  }

  @AfterEach
  public void tearDown() {
    properties.clear();
  }

  @Test
  void whenDistributionConfigBeanCreatedThenPropertiesLoadedCorrectly() {

    assertNotNull("Configuration should not be null", config);
    assertNotNull("Paths should not be null", config.getPaths());
    assertNull("TestData should be null", config.getTestData());

    assertEquals("PrivateKey path value should be loaded correctly.",
        properties.getProperty("services.distribution.paths.privatekey"),
        config.getPrivatekeyPath());
    assertEquals("Certificate path value should be loaded correctly.",
        properties.getProperty("services.distribution.paths.certificate"),
        config.getCertificatePath());
    assertEquals("Output path value should be loaded correctly.",
        properties.getProperty("services.distribution.paths.output"),
        config.getOutputPath());

    assertEquals("Retention Days value should be loaded correctly.",
        properties.getProperty("services.distribution.retention-days"),
        String.valueOf(config.getRetentionDays()));

  }

}