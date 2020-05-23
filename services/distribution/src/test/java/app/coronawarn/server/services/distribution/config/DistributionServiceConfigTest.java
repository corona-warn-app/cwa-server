package app.coronawarn.server.services.distribution.config;

import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertNull;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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
  private DistributionServiceConfig dsc;

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

    assertNotNull("Configuration should not be null", dsc);
    assertNotNull("Paths should not be null", dsc.getPaths());
    assertNull("TestData should be null", dsc.getTestData());
    assertNotNull("Signature should not be null", dsc.getSignature());
    assertNotNull("API should not be null", dsc.getApi());

    Map<String, String> cp = new HashMap<>();
    cp.put("services.distribution.retention-days", dsc.getRetentionDays().toString());
    cp.put("services.distribution.output-file-name", dsc.getOutputFileName());
    cp.put("services.distribution.tek-export.file-name", dsc.getTekExport().getFileName());
    cp.put("services.distribution.tek-export.file-header", dsc.getTekExport().getFileHeader());
    cp.put("services.distribution.tek-export.file-header-width", dsc.getTekExport().getFileHeaderWidth().toString());
    cp.put("services.distribution.paths.privatekey", dsc.getPaths().getPrivateKey());
    cp.put("services.distribution.paths.certificate", dsc.getPaths().getCertificate());
    cp.put("services.distribution.paths.output", dsc.getPaths().getOutput());
    cp.put("services.distribution.signature.app-bundle-id", dsc.getSignature().getAppBundleId());
    cp.put("services.distribution.signature.android-package", dsc.getSignature().getAndroidPackage());
    cp.put("services.distribution.signature.verification-key-id", dsc.getSignature().getVerificationKeyId());
    cp.put("services.distribution.signature.verification-key-version", dsc.getSignature().getVerificationKeyVersion());
    cp.put("services.distribution.signature.algorithm-oid", dsc.getSignature().getAlgorithmOid());
    cp.put("services.distribution.signature.algorithm-name", dsc.getSignature().getAlgorithmName());
    cp.put("services.distribution.signature.file-name", dsc.getSignature().getFileName());
    cp.put("services.distribution.signature.security-provider", dsc.getSignature().getSecurityProvider());
    cp.put("services.distribution.api.version-path", dsc.getApi().getVersionPath());
    cp.put("services.distribution.api.version-v1", dsc.getApi().getVersionV1());
    cp.put("services.distribution.api.country-path", dsc.getApi().getCountryPath());
    cp.put("services.distribution.api.country-germany", dsc.getApi().getCountryGermany());
    cp.put("services.distribution.api.date-path", dsc.getApi().getDatePath());
    cp.put("services.distribution.api.hour-path", dsc.getApi().getHourPath());
    cp.put("services.distribution.api.diagnosis-keys-path", dsc.getApi().getDiagnosisKeysPath());
    cp.put("services.distribution.api.parameters-path", dsc.getApi().getParametersPath());
    cp.put("services.distribution.api.parameters-exposure-configuration-file-name",
        dsc.getApi().getParametersExposureConfigurationFileName());
    cp.put("services.distribution.api.parameters-risk-score-classification-file-name",
        dsc.getApi().getParametersRiskScoreClassificationFileName());
    cp.put("services.distribution.objectstore.access-key", dsc.getObjectStore().getAccessKey());
    cp.put("services.distribution.objectstore.secret-key", dsc.getObjectStore().getSecretKey());
    cp.put("services.distribution.objectstore.endpoint", dsc.getObjectStore().getEndpoint());
    cp.put("services.distribution.objectstore.bucket", dsc.getObjectStore().getBucket());
    cp.put("services.distribution.objectstore.port", dsc.getObjectStore().getPort().toString());
    cp.put("services.distribution.objectstore.set-public-read-acl-on-put-object",
        dsc.getObjectStore().isSetPublicReadAclOnPutObject().toString());

    cp.forEach((key, value) -> assertThat(properties.getProperty(key)).isEqualTo(value));
  }

}
