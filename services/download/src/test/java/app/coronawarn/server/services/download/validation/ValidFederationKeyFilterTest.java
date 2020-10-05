package app.coronawarn.server.services.download.validation;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.services.download.FederationBatchTestHelper;

class ValidFederationKeyFilterTest {

  @ParameterizedTest
  @ValueSource(ints = {-15, -17, 4001})
  void checkFilterRejectsWhenDaysSinceOnsetOfSymptomsNotInRange(int invalidDsos) {
    ValidFederationKeyFilter validator = new ValidFederationKeyFilter();
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKey("test-keydata", invalidDsos);

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @Test
  void checkFilterRejectsWhenDaysSinceOnsetOfSymptomsIsMissing() {
    ValidFederationKeyFilter validator = new ValidFederationKeyFilter();
    DiagnosisKey mockedFederationKey = FederationBatchTestHelper
        .createFederationDiagnosisKeyWithoutDaysSinceSymptoms("test-keydata");

    assertThat(validator.isValid(mockedFederationKey)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 8, -14, 3986})
  void checkFilterAcceptsWhenDaysSinceOnsetOfSymptomsInRange(int validDsos) {
    ValidFederationKeyFilter validator = new ValidFederationKeyFilter();
    DiagnosisKey mockedFederationKey =
        FederationBatchTestHelper.createFederationDiagnosisKey("test-keydata", validDsos);

    assertThat(validator.isValid(mockedFederationKey)).isTrue();
  }
}
