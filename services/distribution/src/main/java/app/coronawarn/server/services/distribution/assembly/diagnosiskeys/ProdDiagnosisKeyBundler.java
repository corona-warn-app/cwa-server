

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.common.ExpirationPolicy;
import app.coronawarn.server.common.persistence.service.common.KeySharingPoliciesChecker;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.LongStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}, that will be distributed while
 * respecting expiry policy (keys must be expired for a configurable amount of time before distribution) and shifting
 * policy (there must be at least a configurable number of keys in a distribution). The policies are configurable
 * through the properties {@code expiry-policy-minutes} and {@code shifting-policy-threshold}.
 */
@Profile("!demo")
@Component
public class ProdDiagnosisKeyBundler extends DiagnosisKeyBundler {

  private KeySharingPoliciesChecker sharingPoliciesChecker;
  private String originCountry;
  private boolean applyPoliciesForAllCountries;

  /**
   * Creates a new {@link ProdDiagnosisKeyBundler}.
   */
  public ProdDiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig,
      KeySharingPoliciesChecker sharingPoliciesChecker) {
    super(distributionServiceConfig);
    this.sharingPoliciesChecker = sharingPoliciesChecker;
    this.originCountry = distributionServiceConfig.getApi().getOriginCountry();
    this.applyPoliciesForAllCountries = distributionServiceConfig.getApplyPoliciesForAllCountries();
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, grouping the diagnosis keys based on the country
   * and by the date on which they may be distributed, while respecting the expiry and shifting policies.
   */
  @Override
  protected void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys) {
    mapDiagnosisKeysPerVisitedCountries(diagnosisKeys).keySet().forEach(country -> {
      if (!country.equals(originCountry) && !applyPoliciesForAllCountries) {
        populateDistributableDiagnosisKeysWithoutPolicies(country);
      } else {
        populateDistributableDiagnosisKeysWithPolicies(country);
      }
    });
    populateEuPackageWithDistributableDiagnosisKeys();
  }

  private void populateDistributableDiagnosisKeysWithPolicies(String country) {

    Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeysGroupedByExpiryPolicy = new HashMap<>(
        groupedDiagnosisKeys.get(country).stream().collect(groupingBy(this::getDistributionDateTimeByExpiryPolicy)));

    if (distributableDiagnosisKeysGroupedByExpiryPolicy.isEmpty()) {
      return;
    }

    LocalDateTime earliestDistributableTimestamp =
        getEarliestDistributableTimestamp(distributableDiagnosisKeysGroupedByExpiryPolicy).orElseThrow();
    LocalDateTime latestDistributableTimestamp = distributionTime;

    List<DiagnosisKey> diagnosisKeyAccumulator = new ArrayList<>();
    LongStream.range(0, earliestDistributableTimestamp.until(latestDistributableTimestamp, ChronoUnit.HOURS))
        .forEach(hourCounter -> {
          LocalDateTime currentHour = earliestDistributableTimestamp.plusHours(hourCounter);
          Collection<DiagnosisKey> currentHourDiagnosisKeys = Optional
              .ofNullable(distributableDiagnosisKeysGroupedByExpiryPolicy.get(currentHour))
              .orElse(emptyList());
          diagnosisKeyAccumulator.addAll(currentHourDiagnosisKeys);
          if (diagnosisKeyAccumulator.size() >= minNumberOfKeysPerBundle) {
            distributableDiagnosisKeys.get(country).put(currentHour, new ArrayList<>(diagnosisKeyAccumulator));
            diagnosisKeyAccumulator.clear();
          } else {
            // placeholder list is needed to be able to generate empty file - see issue #650
            distributableDiagnosisKeys.get(country).put(currentHour, Collections.emptyList());
          }
        });
  }

  private void populateDistributableDiagnosisKeysWithoutPolicies(String country) {
    distributableDiagnosisKeys.get(country).putAll(groupedDiagnosisKeys.get(country).stream()
        .filter(diagnosisKey -> getSubmissionDateTime(diagnosisKey).isBefore(distributionTime))
        .collect(groupingBy(this::getSubmissionDateTime)));
  }

  private static Optional<LocalDateTime> getEarliestDistributableTimestamp(
      Map<LocalDateTime, List<DiagnosisKey>> distributableDiagnosisKeys) {
    return distributableDiagnosisKeys.keySet().stream().min(LocalDateTime::compareTo);
  }

  /**
   * Calculates the earliest point in time at which the specified {@link DiagnosisKey} can be distributed, while
   * respecting the expiry policy and the submission timestamp. Before keys are allowed to be distributed, they must be
   * expired for a configured amount of time.
   *
   * @return {@link LocalDateTime} at which the specified {@link DiagnosisKey} can be distributed.
   */
  private LocalDateTime getDistributionDateTimeByExpiryPolicy(DiagnosisKey diagnosisKey) {
    return sharingPoliciesChecker.getEarliestTimeForSharingKey(diagnosisKey,
        ExpirationPolicy.of(expiryPolicyMinutes, ChronoUnit.MINUTES));
  }
}
