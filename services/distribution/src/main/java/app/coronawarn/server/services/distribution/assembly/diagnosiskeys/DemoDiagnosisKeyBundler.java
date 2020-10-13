

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * An instance of this class contains a collection of {@link DiagnosisKey DiagnosisKeys}, that will be distributed in
 * the same hour they have been submitted.
 */
@Profile("demo")
@Component
public class DemoDiagnosisKeyBundler extends DiagnosisKeyBundler {

  public DemoDiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig);
  }

  /**
   * Initializes the internal {@code distributableDiagnosisKeys} map, grouping the diagnosis keys by country and the
   * submission timestamp, thus ignoring the expiry and shifting policies.
   */
  @Override
  protected void createDiagnosisKeyDistributionMap(Collection<DiagnosisKey> diagnosisKeys) {
    mapDiagnosisKeysPerVisitedCountries(diagnosisKeys)
        .forEach((country, diagnosisKeysPerCountry) ->
            this.distributableDiagnosisKeys.get(country).putAll(diagnosisKeysPerCountry.stream()
                .collect(groupingBy(this::getSubmissionDateTime))));
    populateEuPackageWithDistributableDiagnosisKeys();
  }
}
