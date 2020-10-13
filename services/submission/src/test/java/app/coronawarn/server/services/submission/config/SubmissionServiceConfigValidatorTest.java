

package app.coronawarn.server.services.submission.config;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.domain.config.TekFieldDerivations;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
class SubmissionServiceConfigValidatorTest {

  @Autowired
  private SubmissionServiceConfigValidator submissionServiceConfigValidator;

  private SubmissionServiceConfig submissionServiceConfig;

  @BeforeEach
  void setup() {
    submissionServiceConfig = new SubmissionServiceConfig();
  }

  @ParameterizedTest
  @MethodSource("validRequestDataSizes")
  void testWithValidResquestDataSizes(DataSize dataSize) {
    Errors errors = validateConfig(dataSize, "DE", getEmptyTekFieldDerivations());
    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("invalidRequestDataSizes")
  void testWithInvalidResquestDataSizes(DataSize dataSize) {
    Errors errors = validateConfig(dataSize, "DE", getEmptyTekFieldDerivations());
    assertThat(errors.hasErrors()).isTrue();
  }

  @ParameterizedTest
  @MethodSource("invalidSupportedCountries")
  void testWithInvalidSupportedCountries(String supportedCountries) {
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, supportedCountries,
        getEmptyTekFieldDerivations());
    assertThat(errors.hasErrors()).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"DE", "DE,FR"})
  void testWithValidSupportedCountries(String supportedCountries) {
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, supportedCountries,
        getEmptyTekFieldDerivations());
    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("validTrlFromDsosDatasets")
  void testWithValidTrlFromDsos(Map<Integer, Integer> trlFromDsos) {
    TekFieldDerivations tekFieldDerivations = TekFieldDerivations.from(Map.of(),trlFromDsos, 1);
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, "DE",
        tekFieldDerivations);
    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("invalidTrlFromDsosDatasets")
  void testWithInvalidTrlFromDsos(Map<Integer, Integer> trlFromDsos) {
    TekFieldDerivations tekFieldDerivations = TekFieldDerivations.from(Map.of(),trlFromDsos, 1);
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, "DE",
        tekFieldDerivations);
    assertThat(errors.hasErrors()).isTrue();
  }

  @ParameterizedTest
  @MethodSource("validDsosFromTrlDatasets")
  void testWithValidDsosFromTrl(Map<Integer, Integer> dsosFromTrl) {
    TekFieldDerivations tekFieldDerivations = TekFieldDerivations.from(dsosFromTrl, Map.of(), 1);
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, "DE",
        tekFieldDerivations);
    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("invalidDsosFromTrlDatasets")
  void testWithInvalidDsosFromTrl(Map<Integer, Integer> dsosFromTrl) {
    TekFieldDerivations tekFieldDerivations = TekFieldDerivations.from(dsosFromTrl, Map.of(), 1);
    Errors errors = validateConfig(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE, "DE",
        tekFieldDerivations);
    assertThat(errors.hasErrors()).isTrue();
  }

  private Errors validateConfig(DataSize dataSize, String supportedCountries, TekFieldDerivations tekFieldDerivations) {
    String[] supportedCountriesList = supportedCountries.split(",");
    Errors errors = new BeanPropertyBindingResult(submissionServiceConfig, "submissionServiceConfig");
    submissionServiceConfig.setMaximumRequestSize(dataSize);
    submissionServiceConfig.setPayload(new Payload());
    submissionServiceConfig.setTekFieldDerivations(tekFieldDerivations);
    submissionServiceConfig.setSupportedCountries(supportedCountriesList);
    submissionServiceConfigValidator.validate(submissionServiceConfig, errors);
    return errors;
  }

  private static Stream<Arguments> validRequestDataSizes() {
    return Stream.of(
        SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE,
        SubmissionServiceConfigValidator.MIN_MAXIMUM_REQUEST_SIZE
    ).map(Arguments::of);
  }

  private static Stream<Arguments> invalidRequestDataSizes() {
    return Stream.of(
        DataSize.ofBytes(SubmissionServiceConfigValidator.MAX_MAXIMUM_REQUEST_SIZE.toBytes() + 1),
        DataSize.ofBytes(SubmissionServiceConfigValidator.MIN_MAXIMUM_REQUEST_SIZE.toBytes() - 1)
    ).map(Arguments::of);
  }

  private TekFieldDerivations getEmptyTekFieldDerivations() {
    TekFieldDerivations tekFieldDerivations = TekFieldDerivations.from(Map.of(), Map.of(), 1);
    return tekFieldDerivations;
  }

  private static Stream<Arguments> invalidSupportedCountries() {
    return Stream.of(
        Arguments.of("DE,FRE"),
        Arguments.of("DE, "),
        Arguments.of("de"),
        Arguments.of("dE"),
        Arguments.of("De"),
        Arguments.of(" "),
        Arguments.of(""),
        Arguments.of("\\")
    );
  }

  private static Stream<Arguments> validTrlFromDsosDatasets() {
    Map<Integer, Integer> validMapping1 = Stream.of(new Integer[][]{
        {14, 1},
        {13, 1},
        {3, 3},
        {0, 8},
        {-1, 6},
        {-3, 3},
        {-14, 1}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> validMapping2 = Stream.of(new Integer[][]{
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

  private static Stream<Arguments> invalidTrlFromDsosDatasets() {
    return Stream.of(
        Arguments.of(Map.of(4001, 1)),
        Arguments.of(Map.of(14, 9)),
        Arguments.of(Map.of(14, DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL - 1)),
        Arguments.of(Map.of(-15, 1))
    );
  }

  private static Stream<Arguments> validDsosFromTrlDatasets() {
    Map<Integer, Integer> map1 = Stream.of(new Integer[][]{
        {1, -4},
        {3, -3},
        {5, -2},
        {6, -1},
        {8, 0}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    Map<Integer, Integer> map2 = Stream.of(new Integer[][]{
        {1, -14},
        {2, -3},
        {3, -2},
        {4, 0},
        {5, 12}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    return Stream.of(
        Arguments.of(map1),
        Arguments.of(map2)
    );
  }

  private static Stream<Arguments> invalidDsosFromTrlDatasets() {
    return Stream.of(
        Arguments.of(Map.of(DiagnosisKey.MIN_TRANSMISSION_RISK_LEVEL - 1, -4)),
        Arguments.of(Map.of(1, -15))
    );
  }
}

