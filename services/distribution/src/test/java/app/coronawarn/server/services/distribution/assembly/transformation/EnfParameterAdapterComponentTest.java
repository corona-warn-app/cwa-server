package app.coronawarn.server.services.distribution.assembly.transformation;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;

@EnableConfigurationProperties(value = TransmissionRiskLevelEncoding.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EnfParameterAdapter.class}, initializers = ConfigFileApplicationContextInitializer.class)
class EnfParameterAdapterComponentTest {

  @Autowired
  private EnfParameterAdapter parameterAdapter;

  @ParameterizedTest
  @MethodSource("createTestExpectations")
  void dsosAndReportTypeOfKeysShouldBeAdapted(Integer trl, Integer expectedDsos,
      ReportType expectedReportType) {
    List<DiagnosisKey> keys = buildDiagnosisKeys(6, 51L, 5, "DE", Set.of("DE", "FR"),
        ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 10, trl);

    Collection<DiagnosisKey> adaptedKeys = parameterAdapter.adaptKeys(keys);

    adaptedKeys.forEach(adaptedKey -> {
      assertEquals(expectedDsos, adaptedKey.getDaysSinceOnsetOfSymptoms());
      assertEquals(expectedReportType, adaptedKey.getReportType());
    });
  }

  private static Stream<Arguments> createTestExpectations() {
    return Stream.of(
        /* First argument is TRL, followed by expected DSOS, expected Report Type */
        Arguments.of(1, 1, ReportType.CONFIRMED_TEST),
        Arguments.of(2, 1, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS),
        Arguments.of(3, 1, ReportType.SELF_REPORT),
        Arguments.of(4, 1, ReportType.RECURSIVE),
        Arguments.of(5, 2, ReportType.CONFIRMED_TEST),
        Arguments.of(6, 2, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS),
        Arguments.of(7, 2, ReportType.SELF_REPORT),
        Arguments.of(8, 2, ReportType.RECURSIVE));
  }
}
