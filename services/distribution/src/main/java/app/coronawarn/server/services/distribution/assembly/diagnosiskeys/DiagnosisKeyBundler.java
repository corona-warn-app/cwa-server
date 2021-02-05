

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}.
 */
public abstract class DiagnosisKeyBundler {

  private static final Logger logger = LoggerFactory.getLogger(DiagnosisKeyBundler.class);

  /**
   * The submission timestamp is counted in 1 hour intervals since epoch.
   */
  public static final long ONE_HOUR_INTERVAL_SECONDS = TimeUnit.HOURS.toSeconds(1);

  /**
   * The rolling start interval number is counted in 10 minute intervals since epoch.
   */
  public static final long TEN_MINUTES_INTERVAL_SECONDS = TimeUnit.MINUTES.toSeconds(10);

  protected final long expiryPolicyMinutes;
  protected final int minNumberOfKeysPerBundle;
  private final int maxNumberOfKeysPerBundle;
  protected final List<String> supportedCountries;
  private final String euPackageName;
  private final String originCountry;
  /**
   * The hour at which the distribution runs. This field is needed to prevent the run from distributing any keys that
   * have already been submitted but may only be distributed in the future (e.g. because they are not expired yet).
   */
  protected LocalDateTime distributionTime;

  /**
   * A map containing diagnosis keys, grouped by country and mapped by the LocalDateTime on which they may be
   * distributed.
   */
  protected final Map<String, Map<LocalDateTime, List<DiagnosisKey>>> distributableDiagnosisKeys = new HashMap<>();

  /**
   * A map containing diagnosis keys, grouped by country code.
   */
  protected Map<String, List<DiagnosisKey>> groupedDiagnosisKeys = new HashMap<>();

  /**
   * Constructs a DiagnosisKeyBundler based on the specified service configuration.
   *
   * @param distributionServiceConfig configuration containing relevant attributes
   */
  protected DiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    this.supportedCountries = List.of(distributionServiceConfig.getSupportedCountries());
    this.expiryPolicyMinutes = distributionServiceConfig.getExpiryPolicyMinutes();
    this.minNumberOfKeysPerBundle = distributionServiceConfig.getShiftingPolicyThreshold();
    this.maxNumberOfKeysPerBundle = distributionServiceConfig.getMaximumNumberOfKeysPerBundle();
    this.euPackageName = distributionServiceConfig.getEuPackageName();
    this.originCountry = distributionServiceConfig.getApi().getOriginCountry();
  }

  /**
   * Sets the {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler} and the time at which the
   * distribution runs and calls {@link DiagnosisKeyBundler#createDiagnosisKeyDistributionMap}.
   *
   * @param diagnosisKeys    The {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler}.
   * @param distributionTime The {@link LocalDateTime} at which the distribution runs.
   */
  public void setDiagnosisKeys(Collection<DiagnosisKey> diagnosisKeys, LocalDateTime distributionTime) {
    this.distributionTime = distributionTime;
    this.createDiagnosisKeyDistributionMap(diagnosisKeys);
  }

  /**
   * Returns the {@link LocalDateTime} at which the distribution runs.
   *
   * @return time the distribution runs in LocalDateTime
   */
  public LocalDateTime getDistributionTime() {
    return this.distributionTime;
  }

  /**
   * Returns all {@link DiagnosisKey DiagnosisKeys} contained by this {@link DiagnosisKeyBundler}.
   *
   * @param country to obtain keys for
   * @return list of all DiagnosisKey entries for specified country
   */
  public List<DiagnosisKey> getAllDiagnosisKeys(String country) {
    if (isCountrySupported(country)) {
      return this.distributableDiagnosisKeys.get(country).values()
          .stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    return emptyList();
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, which should contain all diagnosis keys, grouped
   * by the LocalDateTime on which they may be distributed.
   *
   * @param diagnosisKeys collection of DiagnosisKey entries
   */
  protected abstract void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys);

  /**
   * Returns a set of all {@link LocalDate dates} on which {@link DiagnosisKey diagnosis keys} shall be distributed
   * based on country.
   *
   * @param country Country to collect data for
   * @return set of LocalDate entries for the specified Country
   */
  public Set<LocalDate> getDatesWithDistributableDiagnosisKeys(String country) {
    if (isCountrySupported(country)) {
      return this.distributableDiagnosisKeys.get(country).keySet().stream()
          .map(LocalDateTime::toLocalDate)
          .filter(date -> numberOfKeysForDateBelowMaximum(date, country))
          .collect(Collectors.toSet());
    }
    return emptySet();
  }

  public boolean numberOfKeysForDateBelowMaximum(LocalDate date, String country) {
    return numberOfKeysBelowMaximum(getDiagnosisKeysForDate(date, country).size(), date);
  }

  /**
   * Returns a map of all {@link LocalDateTime hours} of a specified {@link LocalDate date} and country during which
   * {@link DiagnosisKey diagnosis keys} shall be distributed.
   *
   * @param currentDate current time
   * @param country Country to search for data relating to
   * @return Set of LocalDateTime entries for specified Country at specified time
   */
  public Set<LocalDateTime> getHoursWithDistributableDiagnosisKeys(LocalDate currentDate, String country) {
    return this.distributableDiagnosisKeys.get(country).keySet().stream()
        .filter(dateTime -> dateTime.toLocalDate().equals(currentDate))
        .filter(dateTime -> numberOfKeysForHourBelowMaximum(dateTime, country))
        .collect(Collectors.toSet());
  }

  private boolean numberOfKeysForHourBelowMaximum(LocalDateTime hour, String country) {
    return numberOfKeysBelowMaximum(getDiagnosisKeysForHour(hour, country).size(), hour);
  }

  private boolean numberOfKeysBelowMaximum(int numberOfKeys, Temporal time) {
    if (numberOfKeys > maxNumberOfKeysPerBundle) {
      logger.error("Number of diagnosis keys ({}) for {} exceeds the configured maximum.", numberOfKeys, time);
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns the submission timestamp of a {@link DiagnosisKey} as a {@link LocalDateTime}.
   *
   * @param diagnosisKey key to search
   * @return time the specified key was submitted
   */
  protected LocalDateTime getSubmissionDateTime(DiagnosisKey diagnosisKey) {
    return LocalDateTime.ofEpochSecond(diagnosisKey.getSubmissionTimestamp() * ONE_HOUR_INTERVAL_SECONDS, 0, UTC);
  }

  /**
   * Returns all diagnosis keys that should be distributed on a specific date for a specific country.
   *
   * @param date date to search
   * @param country country to search
   * @return list of DiagnosisKey entires submitted from specified Country on the specified day
   */
  public List<DiagnosisKey> getDiagnosisKeysForDate(LocalDate date, String country) {
    if (isCountrySupported(country)) {
      return this.distributableDiagnosisKeys.get(country).keySet().stream()
          .filter(dateTime -> dateTime.toLocalDate().equals(date))
          .map(dateTime -> getDiagnosisKeysForHour(dateTime, country))
          .flatMap(List::stream)
          .collect(Collectors.toList());
    }
    return emptyList();
  }

  /**
   * Returns all diagnosis keys that should be distributed in a specific hour for a specific country.
   *
   * @param hour hour to search
   * @param country Country to search
   * @return list of DiagnosisKey entries matching the specified hour and specified Country
   */
  public List<DiagnosisKey> getDiagnosisKeysForHour(LocalDateTime hour, String country) {
    if (isCountrySupported(country)) {
      return Optional
          .ofNullable(this.distributableDiagnosisKeys.get(country).get(hour))
          .orElse(emptyList());
    }
    return emptyList();
  }

  private boolean isCountrySupported(String country) {
    if (!supportedCountries.contains(country) && !country.equals(euPackageName)) {
      logger.warn("The country {} received is not included in the list of supported countries", country);
      return false;
    }
    return true;
  }

  private void addKeyToMap(DiagnosisKey key, Map<String, List<DiagnosisKey>> keysByCountry) {
    // Prior to 1.5 version the already stored keys have no visited countries, thus we default the target bucket
    // to origin country, as these keys were originated in CWA and should still be distributed.
    if (key.getVisitedCountries().isEmpty()) {
      keysByCountry.get(this.originCountry).add(key);
    } else {
      key.getVisitedCountries().stream()
          .filter(supportedCountries::contains)
          .forEach(visitedCountry -> {
            if (isKeyOriginAndVisitedCountryNotEqualToOriginCountry(key, visitedCountry)) {
              return;
            }
            if (isEfgsKeyWithOriginInVisitedCountriesAndNotVisitedCountry(key, visitedCountry)) {
              return;
            }
            keysByCountry.get(visitedCountry).add(key);
          });
    }
  }

  /**
   *  Check if the origin country of the key equals the distribution configuration originCountry
   *  and the current visited country is not equal to the configuration originCountry. This ensures that origin country
   *  keys are only being mapped to their respective origin country bucket. Therefore, we ensure that the policies
   *  for the origin country keys are applied and they don't get distributed in the EUR package.
   * @param key Diagnosis key
   * @param visitedCountry Single entry of visitedCountries list.
   * @return
   */
  private boolean isKeyOriginAndVisitedCountryNotEqualToOriginCountry(DiagnosisKey key, String visitedCountry) {
    return key.getOriginCountry().equals(originCountry) && !visitedCountry.equals(originCountry);
  }

  /**
   *  Check if the origin country of the key does not equal the distribution configuration originCountry,
   *  the current visited country is not equal to the configuration originCountry and the list of visited countries
   *  contain the configuration originCountry. This ensures that the EFGS keys of other supported countries which
   *  contain the configuration originCountry in their visited country list will only be distributed if the
   *  respective originCountry bucket meets all policies and is able to be distributed. Therefore,
   *  we ensure that the keys are not distributed twice and that the packages are in sync for the configuration
   *  originCountry package and the EUR package.
   * @param key Diagnosis key
   * @param visitedCountry Single entry of visitedCountries list.
   * @return
   */
  private boolean isEfgsKeyWithOriginInVisitedCountriesAndNotVisitedCountry(DiagnosisKey key, String visitedCountry) {
    return !key.getOriginCountry().equals(originCountry) && !visitedCountry.equals(originCountry)
        && key.getVisitedCountries().contains(originCountry);
  }

  protected Map<String, List<DiagnosisKey>> mapDiagnosisKeysPerVisitedCountries(
      Collection<DiagnosisKey> diagnosisKeys) {
    initializeMappings();

    diagnosisKeys
        .forEach(diagnosisKey -> this.addKeyToMap(diagnosisKey, groupedDiagnosisKeys));
    return groupedDiagnosisKeys;
  }

  protected void populateEuPackageWithDistributableDiagnosisKeys() {
    Map<LocalDateTime, Set<DiagnosisKey>> euPackage = new HashMap<>();

    distributableDiagnosisKeys
        .forEach((country, diagnosisKeyMap) -> diagnosisKeyMap.forEach((distributionDateTime, diagnosisKeys) -> {
          Set<DiagnosisKey> currentHourDiagnosisKeys = Optional
              .ofNullable(euPackage.get(distributionDateTime))
              .orElse(new HashSet<>());
          currentHourDiagnosisKeys.addAll(diagnosisKeys);
          euPackage.put(distributionDateTime, currentHourDiagnosisKeys);
        }));

    Map<LocalDateTime, List<DiagnosisKey>> euPackageList = new HashMap<>();
    euPackage.forEach((distributionDateTime, diagnosisKeys) ->
        euPackageList.put(distributionDateTime, new ArrayList<>(diagnosisKeys)));
    distributableDiagnosisKeys.put(euPackageName, euPackageList);
  }

  private void initializeMappings() {
    groupedDiagnosisKeys.clear();
    distributableDiagnosisKeys.clear();

    supportedCountries.forEach(supportedCountry -> {
      groupedDiagnosisKeys.put(supportedCountry, new ArrayList<>());
      this.distributableDiagnosisKeys.put(supportedCountry, new HashMap<>());
    });
  }
}
