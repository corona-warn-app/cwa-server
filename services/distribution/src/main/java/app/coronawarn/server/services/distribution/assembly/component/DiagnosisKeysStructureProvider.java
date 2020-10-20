

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.DiagnosisKeyBundler;
import app.coronawarn.server.services.distribution.assembly.diagnosiskeys.structure.directory.DiagnosisKeysDirectory;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.util.TimeUtils;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Retrieves stored diagnosis keys and builds a {@link DiagnosisKeysDirectory} with them.
 */
@Component
public class DiagnosisKeysStructureProvider {

  private static final Logger logger = LoggerFactory
      .getLogger(DiagnosisKeysStructureProvider.class);

  private final DiagnosisKeyBundler diagnosisKeyBundler;
  private final DiagnosisKeyService diagnosisKeyService;
  private final CryptoProvider cryptoProvider;
  private final DistributionServiceConfig distributionServiceConfig;

  /**
   * Creates a new DiagnosisKeysStructureProvider.
   */
  DiagnosisKeysStructureProvider(DiagnosisKeyService diagnosisKeyService, CryptoProvider cryptoProvider,
      DistributionServiceConfig distributionServiceConfig, DiagnosisKeyBundler diagnosisKeyBundler) {
    this.diagnosisKeyService = diagnosisKeyService;
    this.cryptoProvider = cryptoProvider;
    this.distributionServiceConfig = distributionServiceConfig;
    this.diagnosisKeyBundler = diagnosisKeyBundler;
  }

  /**
   * Get directory for diagnosis keys from database.
   *
   * @return the directory
   */
  public Directory<WritableOnDisk> getDiagnosisKeys() {
    logger.debug("Querying diagnosis keys from the database...");
    Collection<DiagnosisKey> diagnosisKeys = diagnosisKeyService.getDiagnosisKeys();
    diagnosisKeyBundler.setDiagnosisKeys(resetDaysSinceOnsetOfSymptoms(diagnosisKeys), TimeUtils.getCurrentUtcHour());
    return new DiagnosisKeysDirectory(diagnosisKeyBundler, cryptoProvider, distributionServiceConfig);
  }

  /**
   * For CWA (server and client) version 1.5 keys must be distributed without
   * 'daysSinceOnsetOfSymptoms'. This is due to a problem with Android devices not being able to
   * process values outside of the range [-14..14].
   *
   * <p>Example of problem reported on Android:
   *
   *
   * <p><code>E/ExposureNotification: java.lang.IllegalArgumentException: daysSinceOnsetOfSymptoms (3998)
   * must be >= -14 and <= 14
   *
   * </code>
   *
   * <p>It becomes a problem when distributing keys that were downloaded via the EFGS channel, because
   * the value range for those keys is [-14, 4000]. Please see DSOS specification for EFGS. This
   * method creates a new collection of keys from the original one but with DSOS information set to
   * 0.
   * It is expected to remove this method in future versions where the DSOS range is extended, or
   * normalized to the Exposure Notification spec.
   */
  private List<DiagnosisKey> resetDaysSinceOnsetOfSymptoms(Collection<DiagnosisKey> diagnosisKeys) {
    return diagnosisKeys.stream().map(key -> keyWithZeroDaysSinceSymptoms(key)).collect(Collectors.toList());
  }

  private DiagnosisKey keyWithZeroDaysSinceSymptoms(DiagnosisKey diagnosisKey) {
    return DiagnosisKey.builder()
        .withKeyData(diagnosisKey.getKeyData())
        .withRollingStartIntervalNumber(diagnosisKey.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(diagnosisKey.getTransmissionRiskLevel())
        .withRollingPeriod(diagnosisKey.getRollingPeriod())
        .withCountryCode(diagnosisKey.getOriginCountry())
        .withReportType(diagnosisKey.getReportType())
        .withVisitedCountries(new HashSet<>(diagnosisKey.getVisitedCountries()))
        .withConsentToFederation(diagnosisKey.isConsentToFederation())
        .withDaysSinceOnsetOfSymptoms(0)
        .withSubmissionTimestamp(diagnosisKey.getSubmissionTimestamp())
        .build();
  }
}
