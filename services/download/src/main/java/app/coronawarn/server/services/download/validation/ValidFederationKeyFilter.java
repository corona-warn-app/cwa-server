package app.coronawarn.server.services.download.validation;

import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Responsible for checking fields of the {@link DiagnosisKey} objects contained within batches
 * downloaded from the EFGS. This check is prior to the one executed when building the domain
 * {@link app.coronawarn.server.common.persistence.domain.DiagnosisKey} entity which ensures our
 * data model constraints are not violated for any incoming data channel.
 */
@Component
public class ValidFederationKeyFilter {

  private static final Logger logger = LoggerFactory.getLogger(ValidFederationKeyFilter.class);

  /**
   * Accepts or rejects a key based on the evaluation of the fields against permitted values.
   */
  public boolean isValid(
      app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey federationKey) {
    return hasValidDaysSinceOnsetOfSymptoms(federationKey);
  }

  private boolean hasValidDaysSinceOnsetOfSymptoms(DiagnosisKey federationKey) {
    boolean hasValidDsos = federationKey.hasDaysSinceOnsetOfSymptoms()
        && federationKey.getDaysSinceOnsetOfSymptoms() >= -14
        && federationKey.getDaysSinceOnsetOfSymptoms() <= 4000;
    if (!hasValidDsos) {
      logger.info("Federation DiagnosisKey found with invalid 'daysSinceOnsetOfSymptoms' value {}",
          federationKey.getDaysSinceOnsetOfSymptoms());
    }
    return hasValidDsos;
  }
}
