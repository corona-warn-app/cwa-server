package app.coronawarn.server.services.upload;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.federation.upload.DiagnosisKeyBatchAssembler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DiagnosisKeyBatchAssembler.class}, initializers = ConfigFileApplicationContextInitializer.class)
class DiagnosisKeyBatchAssemblerTest {

  @Autowired
  DiagnosisKeyBatchAssembler diagnosisKeyBatchAssembler;

  @Test
  public void shouldReturnEmptyListIfNoKeysGive() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(emptyList());
    Assertions.assertTrue(result.isEmpty());
  }

  private void assertKeysAreEqual(DiagnosisKey persistenceKey, app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey exportKey) {
    Assertions.assertArrayEquals(persistenceKey.getKeyData(), exportKey.getKeyData().toByteArray(),
        "Key Data should be the same");
    Assertions.assertArrayEquals(persistenceKey.getVisitedCountries().toArray(), exportKey.getVisitedCountriesList().toArray(),
        "Visited countries should be the same");
    Assertions.assertEquals(persistenceKey.getRollingPeriod(), exportKey.getRollingPeriod(),
        "Rolling Period should be the same");
    Assertions.assertEquals(persistenceKey.getReportType(), exportKey.getReportType(),
        "Verification Type should be the same");
    Assertions.assertEquals(persistenceKey.getTransmissionRiskLevel(), exportKey.getTransmissionRiskLevel(),
        "Transmission Risk Level should be the same");
    Assertions.assertEquals(persistenceKey.getOriginCountry(), exportKey.getOrigin(),
        "Origin Country should be the same");
  }

  private DiagnosisKey makeFakeKey(boolean consent) {
    return DiagnosisKey.builder()
        .withKeyData(new byte[16])
        .withRollingStartIntervalNumber(1)
        .withTransmissionRiskLevel(2)
        .withCountryCode("DE")
        .withConsentToFederation(consent)
        .withReportType(ReportType.CONFIRMED_CLINICAL_DIAGNOSIS)
        .withRollingPeriod(144)
        .withSubmissionTimestamp(LocalDateTime.of(2020, 7, 15, 12, 0, 0).toEpochSecond(ZoneOffset.UTC) / 3600)
        .withVisitedCountries(List.of("DE"))
        .build();
  }

  private DiagnosisKey makeFakeKey() {
    return this.makeFakeKey(true);
  }

  @Test
  void shouldReturnSingleKeyInPackage() {
    var fakeKey = makeFakeKey();
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(List.of(fakeKey));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(1, result.get(0).getKeysCount());
    this.assertKeysAreEqual(fakeKey, result.get(0).getKeys(0));
  }

  @Test
  void shouldReturnMultipleKeysInPackage() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(List.of(makeFakeKey(), makeFakeKey()));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(2, result.get(0).getKeysCount());
  }

  @Test
  void shouldNotPackageKeysIfConsentFlagIsNotSet() {
    var result = diagnosisKeyBatchAssembler.assembleDiagnosisKeyBatch(List.of(makeFakeKey(), makeFakeKey(false)));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(1, result.get(0).getKeysCount());
  }


}
