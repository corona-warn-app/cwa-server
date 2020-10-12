package app.coronawarn.server.services.download.validation;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.FederationBatchTestHelper;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.services.download.config.DownloadServiceConfig.Validation;
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
    Validation validation = mock(Validation.class);

    when(validation.getAllowedReportTypes()).thenReturn(List.of(ReportType.CONFIRMED_TEST));
    when(config.getValidation()).thenReturn(validation);
    this.validator = new ValidFederationKeyFilter(config);
  }

  @Test
  void filterAcceptsValidDiagnosisKey() {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createBuilderForValidFederationDiagnosisKey().build();

    assertThat(validator.isValid(mockedFederationKey)).isTrue();
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

  @Test
  void filterAcceptsCorrectKeyLength() {
    ByteString keyData = FederationBatchTestHelper.createByteStringOfLength(16);
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData);
    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @Test
  void filterRejectsStartIntervalNumberNotAdMidnight() {
    int rollingStart = Math.toIntExact(LocalDateTime.of(LocalDate.now(), LocalTime.NOON).toEpochSecond(UTC) / 600L);
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createBuilderForValidFederationDiagnosisKey()
        .setRollingStartIntervalNumber(rollingStart).build();

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }
}
