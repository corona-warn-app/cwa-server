

package app.coronawarn.server.services.download.config;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {DownloadServiceConfigValidator.class})
@DirtiesContext
class DownloadServiceConfigValidatorTest {

  @Autowired
  private DownloadServiceConfigValidator downloadServiceConfigValidator;

  private DownloadServiceConfig downloadServiceConfig;

  @BeforeEach
  void setup() {
    downloadServiceConfig = new DownloadServiceConfig();
  }

  @ParameterizedTest
  @MethodSource("validTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms")
  void testWithValidTrlFromDsos(Map<Integer, Integer> transmissionRiskLevelFromDaysSinceOnsetOfSymptoms) {
    TekFieldDerivations tekFieldDerivations = TekFieldDerivations.from(Map.of(), transmissionRiskLevelFromDaysSinceOnsetOfSymptoms, 1);
    Errors errors = validateConfig(tekFieldDerivations);
    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("invalidTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms")
  void testWithInvalidTrlFromDsos(Map<Integer, Integer> transmissionRiskLevelFromDaysSinceOnsetOfSymptoms) {
    TekFieldDerivations tekFieldDerivations = TekFieldDerivations.from(Map.of(), transmissionRiskLevelFromDaysSinceOnsetOfSymptoms, 1);
    Errors errors = validateConfig(tekFieldDerivations);
    assertThat(errors.hasErrors()).isTrue();
  }

  private Errors validateConfig(TekFieldDerivations tekFieldDerivations) {
    Errors errors = new BeanPropertyBindingResult(downloadServiceConfig, "downloadServiceConfig");
    downloadServiceConfig.setTekFieldDerivations(tekFieldDerivations);
    downloadServiceConfigValidator.validate(downloadServiceConfig, errors);
    return errors;
  }

  private static Stream<Arguments> validTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms() {
    Map<Integer, Integer> validMapping1 = Stream.of(new Integer[][] {
        {14, 1},
        {13, 1},
        {3, 3},
        {0, 8},
        {-1, 6},
        {-3, 3},
        {-14, 1}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> validMapping2 = Stream.of(new Integer[][] {
        {14, 1},
        {13, 2},
        {3, 3},
        {0, 4},
        {-1, 5},
        {-3, 6},
        {-14, 7},
        {-2, 8}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    return Stream.of(
        Arguments.of(validMapping1),
        Arguments.of(validMapping2)
    );
  }

  private static Stream<Arguments> invalidTransmissionRiskLevelFromDaysSinceOnsetOfSymptoms() {
    return Stream.of(
        Arguments.of(Map.of(4001, 1)),
        Arguments.of(Map.of(14, 9)),
        Arguments.of(Map.of(14, DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL - 1)),
        Arguments.of(Map.of(-15, 1))
    );
  }
}
