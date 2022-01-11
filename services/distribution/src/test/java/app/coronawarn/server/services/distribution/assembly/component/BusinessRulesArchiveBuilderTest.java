package app.coronawarn.server.services.distribution.assembly.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.DistributionArchiveSigningDecorator;
import app.coronawarn.server.services.distribution.assembly.structure.archive.decorator.signing.SigningDecoratorOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping;
import app.coronawarn.server.services.distribution.dgc.client.TestDigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessRulesArchiveBuilderTest {

  public static final String ARCHIVE_NAME = "archive-name";
  public static final String EXPORT_BINARY_FILENAME = "export-test.bin";

  @Mock
  TestDigitalCovidCertificateClient testDigitalCovidCertificateClient;

  @Mock
  DistributionServiceConfig distributionServiceConfig;

  @Mock
  CryptoProvider cryptoProvider;

  @Mock
  DigitalGreenCertificateToCborMapping dgcToCborMapping;

  @InjectMocks
  BusinessRulesArchiveBuilder businessRulesArchiveBuilder;

  @Test
  void businessRuleArchiveShouldBeEmptyIfException() throws DigitalCovidCertificateException, FetchBusinessRulesException {
    when(dgcToCborMapping.constructCborRules(any(), any(), any())).thenThrow(DigitalCovidCertificateException.class);

    Optional<Writable<WritableOnDisk>> businessRuleArchive = businessRulesArchiveBuilder
        .setArchiveName(ARCHIVE_NAME)
        .setRuleType(RuleType.BOOSTER_NOTIFICATION)
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setBusinessRuleItemSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRules)
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build();

    assertTrue(businessRuleArchive.isEmpty());
  }

  @Test
  void archiveShouldBeValid() throws DigitalCovidCertificateException, FetchBusinessRulesException {
    byte[] bytesToSign = new byte[]{1, 2, 3, 4};
    when(dgcToCborMapping.constructCborRules(any(), any(), any())).thenReturn(bytesToSign);

    Optional<Writable<WritableOnDisk>> businessRuleArchive = businessRulesArchiveBuilder
        .setArchiveName(ARCHIVE_NAME)
        .setRuleType(RuleType.BOOSTER_NOTIFICATION)
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setBusinessRuleItemSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRules)
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build();

    assertTrue(businessRuleArchive.isPresent());
    assertThat(businessRuleArchive.get()).isInstanceOf(DistributionArchiveSigningDecorator.class);
    assertThat(businessRuleArchive.get()).isInstanceOf(SigningDecoratorOnDisk.class);
    assertThat(((SigningDecoratorOnDisk) businessRuleArchive.get()).getBytesToSign()).isEqualTo(bytesToSign);

    assertTrue(businessRuleArchive.get().isArchive());
    assertEquals(ARCHIVE_NAME, businessRuleArchive.get().getName());
  }

  @Test
  void shouldThrowWhenFieldIsMissing() {
    assertThrows(NullPointerException.class, () -> businessRulesArchiveBuilder
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setBusinessRuleItemSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRules)
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build());

    assertThrows(NullPointerException.class, () -> businessRulesArchiveBuilder
        .setRuleType(RuleType.BOOSTER_NOTIFICATION)
        .setBusinessRuleItemSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRules)
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build());

    assertThrows(NullPointerException.class, () -> businessRulesArchiveBuilder
        .setRuleType(RuleType.BOOSTER_NOTIFICATION)
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setBusinessRuleSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRuleByHash)
        .build());

    assertThrows(NullPointerException.class, () -> businessRulesArchiveBuilder
        .setRuleType(RuleType.BOOSTER_NOTIFICATION)
        .setExportBinaryFilename(EXPORT_BINARY_FILENAME)
        .setBusinessRuleItemSupplier(testDigitalCovidCertificateClient::getBoosterNotificationRules)
        .build());
  }
}
