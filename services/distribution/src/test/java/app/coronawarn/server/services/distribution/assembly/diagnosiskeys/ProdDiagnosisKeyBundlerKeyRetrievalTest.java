

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static app.coronawarn.server.services.distribution.common.Helpers.buildDiagnosisKeys;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableConfigurationProperties(value = DistributionServiceConfig.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DistributionServiceConfig.class, KeySharingPoliciesChecker.class,
    ProdDiagnosisKeyBundler.class},
    initializers = ConfigFileApplicationContextInitializer.class)
class ProdDiagnosisKeyBundlerKeyRetrievalTest {

  private static final String INVALID_COUNTRY = "TR";

  @Autowired
  DistributionServiceConfig distributionServiceConfig;

  @Autowired
  KeySharingPoliciesChecker sharingPolicyChecker;

  DiagnosisKeyBundler bundler;

  @BeforeEach
  void setupAll() {
    bundler = new ProdDiagnosisKeyBundler(distributionServiceConfig, sharingPolicyChecker);
  }

  @Test
  void testGetsAllDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 5),
            buildDiagnosisKeys(6, 51L, 5),
            buildDiagnosisKeys(6, 52L, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(15);
  }

  @Test
  void testGetsAllDiagnosisKeysWithWrongCountry() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 5),
            buildDiagnosisKeys(6, 51L, 5),
            buildDiagnosisKeys(6, 52L, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getAllDiagnosisKeys("TR")).isEmpty();
  }

  @Test
  void testGetAllDiagnosisKeysWhenEmptyVisitedCountries() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 5, Collections.emptySet()),
            buildDiagnosisKeys(6, 51L, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(10);
  }

  @Test
  void testGetDatesForEmptyListWithWrongCountry() {
    bundler.setDiagnosisKeys(emptySet(), LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 1, 0, 0, 0), INVALID_COUNTRY)).isEmpty();
  }

  @Test
  void testGetDatesForEmptyList() {
    bundler.setDiagnosisKeys(emptySet(), LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDatesWithDistributableDiagnosisKeys("DE")).isEmpty();
  }

  @Test
  void testGetsDatesWithDistributableDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 26L, 5),
            buildDiagnosisKeys(6, 50L, 1),
            buildDiagnosisKeys(6, 74L, 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDatesWithDistributableDiagnosisKeys("DE")).containsAll(List.of(
        LocalDate.of(1970, 1, 2),
        LocalDate.of(1970, 1, 4)
    ));
  }

  @ParameterizedTest
  @MethodSource("createDiagnosisKeysForEpochDay0")
  void testGetDatesForEpochDay0(Collection<DiagnosisKey> diagnosisKeys) {
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    var expDates = Set.of(LocalDate.ofEpochDay(2L), LocalDate.ofEpochDay(3L));
    var actDates = bundler.getDatesWithDistributableDiagnosisKeys("DE");
    assertThat(actDates).isEqualTo(expDates);
  }

  private static Stream<Arguments> createDiagnosisKeysForEpochDay0() {
    return Stream.of(
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 0, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 23, 59, 59), 5)
    ).map(Arguments::of);
  }

  @Test
  void testGetDatesFor2Days() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 1, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 1, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    var expDates = Set.of(LocalDate.ofEpochDay(2L), LocalDate.ofEpochDay(3L));
    assertThat(bundler.getDatesWithDistributableDiagnosisKeys("DE")).isEqualTo(expDates);
  }

  @Test
  void testGetDatesForInvalidCountry() {
    bundler.setDiagnosisKeys(emptySet(), LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDatesWithDistributableDiagnosisKeys("TR")).isEmpty();
  }

  @Test
  void testGetHoursForEmptyList() {
    bundler.setDiagnosisKeys(emptySet(), LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getHoursWithDistributableDiagnosisKeys(LocalDate.of(1970, 1, 3), "DE")).isEmpty();
  }

  @Test
  void testGetsHoursWithDistributableDiagnosisKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 5, 0), 1),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 6, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getHoursWithDistributableDiagnosisKeys(LocalDate.of(1970, 1, 2), "DE")).containsAll(List.of(
        LocalDateTime.of(1970, 1, 2, 4, 0, 0),
        LocalDateTime.of(1970, 1, 2, 6, 0, 0)
    ));
  }

  @Test
  void testGetsDiagnosisKeysForDate() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 2, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 3, 2, 0), 1),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, 2, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 20, 0));
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 1), "DE")).isEmpty();
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 2), "DE")).hasSize(5);
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 3), "DE")).isEmpty();
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 4), "DE")).hasSize(6);
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 5), "DE")).isEmpty();
  }

  @Test
  void testEmptyListWhenGettingDiagnosisKeysForDateBeforeEarliestDiagnosisKey() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 1), "DE")).isEmpty();
  }

  @Test
  void testEmptyListWhenInvalidCountry() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 4, 0, 0));
    assertThat(bundler.getDiagnosisKeysForDate(LocalDate.of(1970, 1, 5), "TR")).isEmpty();
  }

  @Test
  void testGetsDiagnosisKeysForHour() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 5, 0), 1),
            buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 6, 0), 5))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 3, 0), "DE")).isEmpty();
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 4, 0), "DE")).hasSize(5);
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 5, 0), "DE")).isEmpty();
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 6, 0), "DE")).hasSize(6);
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 2, 7, 0), "DE")).isEmpty();
  }

  @Test
  void testEmptyListWhenGettingDiagnosisKeysForHourBeforeEarliestDiagnosisKey() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 1, 0, 0, 0), "DE")).isEmpty();
  }

  @Test
  void testGetsCorrectDistributionDate() {
    LocalDateTime expected = LocalDateTime.of(1970, 1, 5, 0, 0);
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, expected);
    assertThat(bundler.getDistributionTime()).isEqualTo(expected);
  }

  @Test
  void testGetDiagnosisKeysForDateWithInvalidCountry() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 1, 0, 0, 0), INVALID_COUNTRY)).isEmpty();
  }

  @Test
  void testGetDiagnosisKeysForHourWithInvalidCountry() {
    List<DiagnosisKey> diagnosisKeys = buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 2, 4, 0), 5);
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));
    assertThat(bundler.getDiagnosisKeysForHour(LocalDateTime.of(1970, 1, 1, 0, 0, 0), INVALID_COUNTRY)).isEmpty();
  }

  @Test
  void testGetsHoursWithDistributableDiagnosisKeysExceedingMaximumNumberOfKeys() {
    DistributionServiceConfig spyConfig = spy(distributionServiceConfig);
    when(spyConfig.getMaximumNumberOfKeysPerBundle()).thenReturn(3);
    when(spyConfig.getShiftingPolicyThreshold()).thenReturn(1);
    DiagnosisKeyBundler keyBundler = new ProdDiagnosisKeyBundler(spyConfig, sharingPolicyChecker);

    List<DiagnosisKey> diagnosisKeys = IntStream.range(0, 24).mapToObj(hour ->
        buildDiagnosisKeys(6, LocalDateTime.of(1970, 1, 4, hour, 0), 4))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    keyBundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 5, 0, 0));

    Set<LocalDateTime> expectedKeys = keyBundler.getHoursWithDistributableDiagnosisKeys(LocalDate.of(1970, 1, 4), "DE");
    assertThat(expectedKeys).isEmpty();
  }

  @Test
  void testIfOriginCountryKeyIsPartOfEuPackage() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 10))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(10);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(10);
  }

  @Test
  void testOriginKeysAndEfgsKeysAreIncludedInEuPackage() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 10, "DE", Set.of("DE"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 10, "FR", Set.of("FR"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(10);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(20);
  }

  @Test
  void testEfgsKeysAreAddedToOriginPackageBasedOnVisitedCountries() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 10, "DE", Set.of("DE"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 10, "FR", Set.of("FR", "DE"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 10, "FR", Set.of("FR"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(20);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(30);
  }

  @Test
  void testOriginCountryKeysAndEfgsKeysWithValidDistribution() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 10, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 10, "FR", Set.of("FR"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(10);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(20);
  }

  @Test
  void testOriginCountryKeysNotExpiredPlusVisitedCountryKeys() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 52L, 10, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 10, "FR", Set.of("FR"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).isEmpty();
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(10);
  }

  @Test
  void testEFGSKeysWithOriginAsVisitedCountryBeforeExpiryTime() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 52L, 10, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 52L, 10, "FR", Set.of("FR", "DE"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).isEmpty();
    assertThat(bundler.getAllDiagnosisKeys("EUR")).isEmpty();
  }

  @Test
  void testEFGSKeysWithOriginAsVisitedCountryNotExceedingThreshold() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 1, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 1, "FR", Set.of("FR", "DE"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).isEmpty();
    assertThat(bundler.getAllDiagnosisKeys("EUR")).isEmpty();
  }

  @Test
  void testEFGSKeysWithOriginAsVisitedCountryDistributedAfterExceedingThreshold() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 1, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 3, "FR", Set.of("FR", "DE"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 24L, 1, "FR", Set.of("FR"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).isEmpty();
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(1);

    diagnosisKeys.addAll(buildDiagnosisKeys(6, 74L, 1, "FR", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0));
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 4, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(5);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(6);
  }

  @Test
  void testEFGSKeysWithOriginAsVisitedCountryDistributedAfterExpiryTime() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 24L, 2, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 24L, 3, "FR", Set.of("FR", "DE"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 24L, 3, "FR", Set.of("FR"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 2, 1, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).isEmpty();
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(3);

    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 2, 5, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(5);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(8);
  }

  @Test
  void testOriginCountryKeysPlusVisitedCountryKeysAmountNotHigherThanThreshold() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 4, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 10, "FR", Set.of("FR"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).isEmpty();
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(10);
  }

  @Test
  void testOriginCountryKeysPlusVisitedCountryKeysAmountHigherThanThreshold() {
    List<DiagnosisKey> diagnosisKeys = Stream
        .of(buildDiagnosisKeys(6, 50L, 4, "DE", Set.of("DE", "FR"), ReportType.CONFIRMED_TEST, 0),
            buildDiagnosisKeys(6, 50L, 10, "FR", Set.of("FR", "DE"), ReportType.CONFIRMED_TEST, 0))
        .flatMap(List::stream)
        .collect(Collectors.toList());
    bundler.setDiagnosisKeys(diagnosisKeys, LocalDateTime.of(1970, 1, 3, 3, 0));
    assertThat(bundler.getAllDiagnosisKeys("DE")).hasSize(14);
    assertThat(bundler.getAllDiagnosisKeys("EUR")).hasSize(14);
  }
}
