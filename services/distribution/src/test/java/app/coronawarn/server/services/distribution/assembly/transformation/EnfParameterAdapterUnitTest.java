package app.coronawarn.server.services.distribution.assembly.transformation;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.distribution.config.TransmissionRiskLevelEncoding;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EnfParameterAdapterUnitTest {

  private static final TransmissionRiskLevelEncoding TEST_ENCODINGS =
      TransmissionRiskLevelEncoding.from(
          Map.of(1, 1, 2, 2, 3, 2, 4, 2, 5, 2, 6, 1, 7, 2, 8, 1),
          Map.of(1, 1, 2, 2, 3, 3, 4, 4, 5, 1, 6, 2, 7, 3, 8, 4));

  @ParameterizedTest
  @MethodSource("createTestExpectations")
  void dsosAndReportTypeOfKeysShouldBeAdapted(Integer trl, Integer expectedDsos,
      ReportType expectedReportType) {
    List<DiagnosisKey> keys = buildDiagnosisKeys(6, 51L, 5, "DE", Set.of("DE", "FR"),
        ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 10, trl);

    EnfParameterAdapter parameterAdapter = new EnfParameterAdapter(TEST_ENCODINGS);
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
        Arguments.of(3, 2, ReportType.SELF_REPORT),
        Arguments.of(5, 2, ReportType.CONFIRMED_TEST),
        Arguments.of(8, 1, ReportType.RECURSIVE));
  }
}
