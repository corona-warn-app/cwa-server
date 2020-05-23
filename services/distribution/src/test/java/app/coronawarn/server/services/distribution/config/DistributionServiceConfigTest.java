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
  private DistributionServiceConfig distributionServiceConfig;

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

    assertNotNull("Configuration should not be null", distributionServiceConfig);
    assertNotNull("Paths should not be null", distributionServiceConfig.getPaths());
    assertNull("TestData should be null", distributionServiceConfig.getTestData());
    assertNotNull("Signature should not be null", distributionServiceConfig.getSignature());

    assertEquals("PrivateKey path value should be loaded correctly.",
      properties.getProperty("services.distribution.paths.privatekey"),
      distributionServiceConfig.getPaths().getPrivateKey());

    assertEquals("Certificate path value should be loaded correctly.",
      properties.getProperty("services.distribution.paths.certificate"),
      distributionServiceConfig.getPaths().getCertificate());

    assertEquals("Output path value should be loaded correctly.",
      properties.getProperty("services.distribution.paths.output"),
      distributionServiceConfig.getPaths().getOutput());

    assertEquals("Retention Days value should be loaded correctly.",
      properties.getProperty("services.distribution.retention-days"),
      String.valueOf(distributionServiceConfig.getRetentionDays()));

    assertEquals("App bundle ID value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.app-bundle-id"),
        String.valueOf(distributionServiceConfig.getSignature().getAppBundleId()));

    assertEquals("Android package value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.android-package"),
        String.valueOf(distributionServiceConfig.getSignature().getAndroidPackage()));

    assertEquals("Verification key ID value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.verification-key-id"),
        String.valueOf(distributionServiceConfig.getSignature().getVerificationKeyId()));

    assertEquals("Verification key version value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.verification-key-version"),
        String.valueOf(distributionServiceConfig.getSignature().getVerificationKeyVersion()));

    assertEquals("Algorithm OID value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.algorithm-oid"),
        String.valueOf(distributionServiceConfig.getSignature().getAlgorithmOid()));

    assertEquals("Algorithm name value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.algorithm-name"),
        String.valueOf(distributionServiceConfig.getSignature().getAlgorithmName()));

    assertEquals("File name value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.file-name"),
        String.valueOf(distributionServiceConfig.getSignature().getFileName()));

    assertEquals("Security provider version value should be loaded correctly.",
        properties.getProperty("services.distribution.signature.security-provider"),
        String.valueOf(distributionServiceConfig.getSignature().getSecurityProvider()));
  }

}
