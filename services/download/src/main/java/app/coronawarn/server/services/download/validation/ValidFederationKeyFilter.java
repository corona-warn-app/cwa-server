package app.coronawarn.server.services.download.validation;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.download.config.DownloadServiceConfig;
import app.coronawarn.server.services.download.config.DownloadServiceConfig.Validation;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for checking fields of the {@link DiagnosisKey} objects contained within batches downloaded from the
 * EFGS. This check is prior to the one executed when building the domain
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey}
 * entity which ensures our data model constraints are not violated for any incoming data channel.
 */
@Component
public class ValidFederationKeyFilter {

  private static final Logger logger = LoggerFactory.getLogger(ValidFederationKeyFilter.class);
  private final int keyLength;
  private final List<ReportType> allowedReportTypes;
  private final int minDsos;
  private final int maxDsos;
  private final int minRollingPeriod;
  private final int maxRollingPeriod;
  private final int minTrl;
  private final int maxTrl;

  /**
   * Constructor for this class.
   *
   * @param downloadServiceConfig A {@link DownloadServiceConfig} object.
   */
  public ValidFederationKeyFilter(DownloadServiceConfig downloadServiceConfig) {
    Validation validation = downloadServiceConfig.getValidation();
    this.keyLength = validation.getKeyLength();
    this.allowedReportTypes = validation.getAllowedReportTypes();
    this.minDsos = validation.getMinDsos();
    this.maxDsos = validation.getMaxDsos();
    this.minRollingPeriod = validation.getMinRollingPeriod();
    this.maxRollingPeriod = validation.getMaxRollingPeriod();
    this.minTrl = validation.getMinTrl();
    this.maxTrl = validation.getMaxTrl();
  }

  /**
   * Accepts or rejects a key based on the evaluation of the fields against permitted values.
   */
  public boolean isValid(DiagnosisKey federationKey) {
    return hasValidDaysSinceOnsetOfSymptoms(federationKey)
        && hasAllowedReportType(federationKey)
        && hasExpectedKeyLength(federationKey)
        && hasValidStartIntervalNumber(federationKey)
        && hasValidTransmissionRiskLevel(federationKey)
        && hasValidRollingPeriod(federationKey);
  }

  private boolean hasValidDaysSinceOnsetOfSymptoms(DiagnosisKey federationKey) {
    boolean hasValidDsos = federationKey.hasDaysSinceOnsetOfSymptoms()
        && federationKey.getDaysSinceOnsetOfSymptoms() >= minDsos
        && federationKey.getDaysSinceOnsetOfSymptoms() <= maxDsos;
    if (!hasValidDsos) {
      logger.info("Filter skipped Federation DiagnosisKey with invalid 'daysSinceOnsetOfSymptoms' value {}.",
          federationKey.getDaysSinceOnsetOfSymptoms());
    }
    return hasValidDsos;
  }

  private boolean hasAllowedReportType(DiagnosisKey federationKey) {
    boolean hasAllowedReportType = allowedReportTypes.contains(federationKey.getReportType());
    if (!hasAllowedReportType) {
      logger.info("Filter skipped Federation DiagnosisKey with invalid 'ReportType' {}.",
          federationKey.getReportType());
    }
    return hasAllowedReportType;
  }

  private boolean hasExpectedKeyLength(DiagnosisKey federationKey) {
    boolean hasCorrectKeyLength = federationKey.getKeyData().toByteArray().length == keyLength;
    if (!hasCorrectKeyLength) {
      logger.info("Filter skipped Federation DiagnosisKey with invalid 'KeyData' length {}.",
          federationKey.getKeyData().toByteArray().length);
    }
    return hasCorrectKeyLength;
  }

  private boolean hasValidStartIntervalNumber(DiagnosisKey federationKey) {
    boolean hasValidStartIntervalNumber = federationKey.hasRollingStartIntervalNumber()
        && rollingStartIntervalNumberIsMidnight(federationKey);

    if (!hasValidStartIntervalNumber) {
      logger.info("Filter skipped Federation DiagnosisKey with rolling start interval number {} not at midnight.",
          federationKey.getRollingStartIntervalNumber());
    }
    return hasValidStartIntervalNumber;
  }

  private boolean rollingStartIntervalNumberIsMidnight(DiagnosisKey federationKey) {
    return federationKey.getRollingStartIntervalNumber() % maxRollingPeriod == 0;
  }

  private boolean hasValidTransmissionRiskLevel(DiagnosisKey federationKey) {
    int trl = federationKey.getTransmissionRiskLevel();
    boolean hasValidTrl = trl >= minTrl && trl <= maxTrl;
    if (!hasValidTrl) {
      logger.info("Filter skipped Federation DiagnosisKey with invalid transmission risk level {}.", trl);
    }
    return hasValidTrl;
  }

  private boolean hasValidRollingPeriod(DiagnosisKey federationKey) {
    int rollingPeriod = federationKey.getRollingPeriod();
    boolean hasValidRollingPeriod = federationKey.hasRollingPeriod()
        && rollingPeriod >= minRollingPeriod
        && rollingPeriod <= maxRollingPeriod;
    if (!hasValidRollingPeriod) {
      logger.info("Filter skipped Federation DiagnosisKey with missing or invalid rolling period {}.",
          rollingPeriod);
    }
    return hasValidRollingPeriod;
  }

}
