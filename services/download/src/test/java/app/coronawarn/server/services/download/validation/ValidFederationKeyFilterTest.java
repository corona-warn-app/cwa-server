package app.coronawarn.server.services.download.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.DownloadServiceConfig;
import app.coronawarn.server.services.download.FederationBatchTestHelper;
import com.google.protobuf.ByteString;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidFederationKeyFilterTest {

  private final ValidFederationKeyFilter validator;

  private final DownloadServiceConfig downloadServiceConfig;

  public ValidFederationKeyFilterTest() {
    this.downloadServiceConfig = mock(DownloadServiceConfig.class);
    when(downloadServiceConfig.getAllowedReportTypesToDownload()).thenReturn(List.of(ReportType.CONFIRMED_TEST));
    this.validator = new ValidFederationKeyFilter(downloadServiceConfig);
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, -17, 4001})
  void checkFilterRejectsWhenDaysSinceOnsetOfSymptomsNotInRange(int invalidDsos) {
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKeyWithDsos(invalidDsos);

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @Test
  void checkFilterRejectsWhenDaysSinceOnsetOfSymptomsIsMissing() {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createFederationDiagnosisKeyWithoutDsos();

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, -14, 3986})
  void checkFilterAcceptsWhenDaysSinceOnsetOfSymptomsInRange(int validDsos) {
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKeyWithDsos(validDsos);

    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = ReportType.class, names = {"CONFIRMED_TEST"})
  void checkFilterAcceptsReportTypes(ReportType reportType) {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithReportType(reportType);
    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = ReportType.class, names = {"UNKNOWN", "CONFIRMED_CLINICAL_DIAGNOSIS", "SELF_REPORT", "RECURSIVE",
      "REVOKED"})
  void checkFilterRejectsReportTypes(ReportType reportType) {
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithReportType(reportType);
    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @Test
  void checkFilterAcceptsCorrectKeyLength() {
    ByteString keyData = FederationBatchTestHelper.createByteStringOfLength(16);
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData);
    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 15, 17, 20})
  void checkFilterRejectsIncorrectKeyLength(int invalidKeyLength) {
    ByteString keyData = FederationBatchTestHelper.createByteStringOfLength(invalidKeyLength);
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper.createFederationDiagnosisKeyWithKeyData(keyData);
    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }
}
