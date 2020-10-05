package app.coronawarn.server.services.download.validation;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.DownloadServiceConfig;
import app.coronawarn.server.services.download.FederationBatchTestHelper;
import com.google.protobuf.ByteString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidFederationKeyFilterTest {

  private final ValidFederationKeyFilter validator;

  public ValidFederationKeyFilterTest() {
    DownloadServiceConfig config = mock(DownloadServiceConfig.class);
    when(config.getKeyLength()).thenReturn(16);
    when(config.getAllowedReportTypesToDownload()).thenReturn(List.of(ReportType.CONFIRMED_TEST));
    when(config.getMinDsos()).thenReturn(-14);
    when(config.getMaxDsos()).thenReturn(4000);
    when(config.getMaxRollingPeriod()).thenReturn(144);
    when(config.getMinTrl()).thenReturn(1);
    when(config.getMaxTrl()).thenReturn(8);
    this.validator = new ValidFederationKeyFilter(config);
  }

  @Test
  void filterAcceptsValidDiagnosisKey() {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createBuilderForValidFederationDiagnosisKey().build();

    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, -17, 4001})
  void filterRejectsWhenDaysSinceOnsetOfSymptomsNotInRange(int invalidDsos) {
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKeyWithDsos(invalidDsos);

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @Test
  void filterRejectsWhenDaysSinceOnsetOfSymptomsIsMissing() {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createFederationDiagnosisKeyWithoutDsos();

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, -14, 3986})
  void filterAcceptsWhenDaysSinceOnsetOfSymptomsInRange(int validDsos) {
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKeyWithDsos(validDsos);

    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = ReportType.class, names = {"CONFIRMED_TEST"})
  void filterAcceptsReportTypes(ReportType reportType) {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithReportType(reportType);
    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = ReportType.class, names = {"UNKNOWN", "CONFIRMED_CLINICAL_DIAGNOSIS", "SELF_REPORT", "RECURSIVE",
      "REVOKED"})
  void filterRejectsReportTypes(ReportType reportType) {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithReportType(reportType);
    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  // TODO what if no report type is provided?

  @Test
  void filterAcceptsCorrectKeyLength() {
    ByteString keyData = FederationBatchTestHelper.createByteStringOfLength(16);
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData);
    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 15, 17, 20})
  void filterRejectsIncorrectKeyLength(int invalidKeyLength) {
    ByteString keyData = FederationBatchTestHelper.createByteStringOfLength(invalidKeyLength);
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData);
    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @Test
  void filterRejectsStartIntervalNumberNotAdMidnight() {
    int rollingStart = Math.toIntExact(LocalDateTime.of(LocalDate.now(), LocalTime.NOON).toEpochSecond(UTC) / 600L);
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createBuilderForValidFederationDiagnosisKey()
        .setRollingStartIntervalNumber(rollingStart).build();

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 9})
  void filterRejectsInvalidTransmissionRiskLevels(int invalidTrl) {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createBuilderForValidFederationDiagnosisKey()
        .setTransmissionRiskLevel(invalidTrl).build();

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @Test
  void filterRejectsMissingRollingPeriod() {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createBuilderForValidFederationDiagnosisKey()
        .clearRollingPeriod()
        .build();

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }
}
