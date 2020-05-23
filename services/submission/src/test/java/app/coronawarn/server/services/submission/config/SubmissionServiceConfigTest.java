package app.coronawarn.server.services.submission.config;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

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
@EnableConfigurationProperties(value = SubmissionServiceConfig.class)
@TestPropertySource("classpath:application-config-test.properties")
public class SubmissionServiceConfigTest {

  @Autowired
  private SubmissionServiceConfig config;

  private final Properties properties = new Properties();

  @BeforeEach
  public void setup() throws IOException {
    InputStream is = getClass().getClassLoader()
        .getResourceAsStream("application-config-test.properties");
    properties.load(is);
  }

  @AfterEach
  public void tearDown() {
    properties.clear();
  }

  @Test
  void whenDistributionConfigBeanCreatedThenPropertiesLoadedCorrectly() {

    assertNotNull("Configuration should not be null", config);

    assertEquals("Fake Delay value should be loaded correctly.",
        properties.getProperty("services.submission.initial_fake_delay_milliseconds"),
        String.valueOf(config.getInitialFakeDelayMilliseconds()));
    assertEquals("Fake Delay Moving Average value should be loaded correctly.",
        properties.getProperty("services.submission.fake_delay_moving_average_samples"),
        String.valueOf(config.getFakeDelayMovingAverageSamples()));
    assertEquals("Retention Days should be loaded correctly.",
        properties.getProperty("services.submission.retention-days"),
        String.valueOf(config.getRetentionDays()));
    assertEquals("Max Number of Days value should be loaded correctly.",
        properties.getProperty("services.submission.payload.max-number-of-keys"),
        String.valueOf(config.getMaxNumberOfKeys()));

  }
}