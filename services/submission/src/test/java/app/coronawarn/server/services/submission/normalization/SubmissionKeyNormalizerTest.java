

package app.coronawarn.server.services.submission.normalization;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.common.persistence.domain.normalization.NormalizableFields;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;

class SubmissionKeyNormalizerTest {

  @ParameterizedTest
  @MethodSource("dsosTrlDummyPairValues")
  void testDsosIsCorrectlyDerived(int inputTrlValue, int expectedDsosValue) {
    SubmissionServiceConfig mockedConfig = mock(SubmissionServiceConfig.class);
    TekFieldDerivations mockedDerivationRules = mock(TekFieldDerivations.class);
    when(mockedConfig.getTekFieldDerivations()).thenReturn(mockedDerivationRules);
    when(mockedDerivationRules.deriveDaysSinceSymptomsFromTransmissionRiskLevel(inputTrlValue)).thenReturn(expectedDsosValue);

    SubmissionKeyNormalizer normalizer = new SubmissionKeyNormalizer(mockedConfig);
    NormalizableFields result = normalizer.normalize(NormalizableFields.of(inputTrlValue, null));
    Assertions.assertThat(result.getDaysSinceOnsetOfSymptoms()).isEqualTo(expectedDsosValue);

    result = normalizer.normalize(NormalizableFields.of(inputTrlValue - 1, null));
    Assertions.assertThat(result.getDaysSinceOnsetOfSymptoms()).isNotEqualTo(expectedDsosValue);
  }

  @ParameterizedTest
  @MethodSource("dsosTrlDummyPairValues")
  void testTrlIsCorrectlyDerived(int inputDsosValue, int expectedTrlValue) {
    SubmissionServiceConfig mockedConfig = mock(SubmissionServiceConfig.class);
    TekFieldDerivations mockedDerivationRules = mock(TekFieldDerivations.class);
    when(mockedConfig.getTekFieldDerivations()).thenReturn(mockedDerivationRules);
    when(mockedDerivationRules.deriveTransmissionRiskLevelFromDaysSinceSymptoms(inputDsosValue)).thenReturn(expectedTrlValue);

    SubmissionKeyNormalizer normalizer = new SubmissionKeyNormalizer(mockedConfig);
    NormalizableFields result = normalizer.normalize(NormalizableFields.of(null, inputDsosValue));
    Assertions.assertThat(result.getTransmissionRiskLevel()).isEqualTo(expectedTrlValue);

    result = normalizer.normalize(NormalizableFields.of(inputDsosValue - 1, null));
    Assertions.assertThat(result.getTransmissionRiskLevel()).isNotEqualTo(expectedTrlValue);
  }

  @Test
  void testErrorIsThrownWhenAllRequiredFieldsForNormalizationAreMissing() {
    SubmissionServiceConfig mockedConfig = mock(SubmissionServiceConfig.class);
    TekFieldDerivations mockedDerivationRules = mock(TekFieldDerivations.class);
    when(mockedConfig.getTekFieldDerivations()).thenReturn(mockedDerivationRules);
    when(mockedDerivationRules.deriveDaysSinceSymptomsFromTransmissionRiskLevel(1)).thenReturn(2);

    SubmissionKeyNormalizer normalizer = new SubmissionKeyNormalizer(mockedConfig);
    NormalizableFields missingValues = NormalizableFields.of(null, null);
    Assertions.assertThatThrownBy(() -> {
      normalizer.normalize(missingValues);
    }).isOfAnyClassIn(IllegalArgumentException.class);
  }

  /**
   * A dummy mapping of TRL/DSOS values that can be used in tests.
   */
  private static Stream<Arguments> dsosTrlDummyPairValues() {
    return Stream.of(
        Arguments.of(1, 14),
        Arguments.of(3, 10),
        Arguments.of(6, 8)
    );
  }
}
