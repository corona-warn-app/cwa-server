package app.coronawarn.server.services.distribution.assembly.component;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = {DistributionServiceConfig.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {DigitalGreenCertificateToCborMapping.class,
        CryptoProvider.class, DistributionServiceConfig.class,
        TestDigitalCovidCertificateClient.class, DigitalGreenCertificateToCborMapping.class,
        CryptoProvider.class,
        BoosterNotificationStructureProvider.class, BusinessRulesArchiveBuilder.class},
    initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles({"fake-dcc-client", "fake-dsc-client"})
class BusinessRulesArchiveBuilderTest {

  public static final String ARCHIVE_NAME = "archive-name";
  public static final String EXPORT_BINARY_FILENAME = "export-test.bin";

  @Autowired
  TestDigitalCovidCertificateClient testDigitalCovidCertificateClient;

  @Autowired
  BusinessRulesArchiveBuilder businessRulesArchiveBuilder;

  @Test
  void testArchiveIsProperlyBuilt() {
    Optional<Writable<WritableOnDisk>> bussinessRuleArchive = businessRulesArchiveBuilder
        .setArchiveName(ARCHIVE_NAME)
        .setRuleType(RuleType.BoosterNotification)
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setBusinessRuleItemSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRules)
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build();

    assertTrue(bussinessRuleArchive.isPresent());
    assertTrue(bussinessRuleArchive.get().isArchive());
    assertEquals(bussinessRuleArchive.get().getName(), ARCHIVE_NAME);
  }

  @Test
  void shouldThrowWhenFieldIsMissing() {
    assertThrows(NullPointerException.class, () -> businessRulesArchiveBuilder
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build());
  }
}
