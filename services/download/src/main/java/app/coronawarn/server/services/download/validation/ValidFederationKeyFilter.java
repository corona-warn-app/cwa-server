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
  private final List<ReportType> allowedReportTypes;

  /**
   * Constructor for this class.
   *
   * @param downloadServiceConfig A {@link DownloadServiceConfig} object.
   */
  public ValidFederationKeyFilter(DownloadServiceConfig downloadServiceConfig) {
    Validation validation = downloadServiceConfig.getValidation();
    this.allowedReportTypes = validation.getAllowedReportTypes();
  }

  /**
   * Accepts or rejects a key based on the evaluation of the fields against permitted values.
   */
  public boolean isValid(DiagnosisKey federationKey) {
    return hasAllowedReportType(federationKey)
        && hasRollingStartIntervalNumberAtMidnight(federationKey);
  }

  private boolean hasAllowedReportType(DiagnosisKey federationKey) {
    boolean hasAllowedReportType = allowedReportTypes.contains(federationKey.getReportType());
    if (!hasAllowedReportType) {
      logger.info("Filter skipped Federation DiagnosisKey with invalid 'ReportType' {}.",
          federationKey.getReportType());
    }
    return hasAllowedReportType;
  }

  private boolean hasRollingStartIntervalNumberAtMidnight(DiagnosisKey federationKey) {
    int rollingStartIntervalNumber = federationKey.getRollingStartIntervalNumber();
    boolean hasRollingStartIntervalNumberAtMidnight = rollingStartIntervalNumber
        % app.coronawarn.server.common.persistence.domain.DiagnosisKey.MAX_ROLLING_PERIOD
        == 0;
    if (!hasRollingStartIntervalNumberAtMidnight) {
      logger.info("Filter skipped Federation DiagnosisKey with rolling start interval number {} not being at midnight.",
          rollingStartIntervalNumber);
    }
    return hasRollingStartIntervalNumberAtMidnight;
  }
}
