package app.coronawarn.server.services.distribution.assembly.diagnosiskeys;

import static java.util.stream.Collectors.groupingBy;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DemoDiagnosisKeyBundler extends DiagnosisKeyBundler {

  public DemoDiagnosisKeyBundler(DistributionServiceConfig distributionServiceConfig) {
    super(distributionServiceConfig);
  }

  @Override
  protected Map<LocalDateTime, List<DiagnosisKey>> createDiagnosisKeyDistributionMap(
      Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream().collect(groupingBy(this::getSubmissionDateTime));
  }

  @Override
  public List<DiagnosisKey> getDiagnosisKeysDistributableAt(LocalDateTime hour) {
    return this.getDiagnosisKeysForHour(hour);
  }
}
