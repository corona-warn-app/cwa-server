

package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    this.distributableDiagnosisKeys.clear();
    Map<String, List<DiagnosisKey>> diagnosisKeysMapped = new HashMap<>();

    groupDiagnosisKeysByCountry(diagnosisKeysMapped);
    mapDiagnosisKeysPerVisitedCountries(diagnosisKeys, diagnosisKeysMapped)
        .forEach((country, diagnosisKeysPerCountry) ->
            this.distributableDiagnosisKeys.get(country).putAll(diagnosisKeysPerCountry.stream()
                .collect(groupingBy(this::getSubmissionDateTime))));
    populateEuPackageWithDistributableDiagnosisKeys();
  }
}
