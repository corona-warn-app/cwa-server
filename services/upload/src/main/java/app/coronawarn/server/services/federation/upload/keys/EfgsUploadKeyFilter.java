package app.coronawarn.server.services.federation.upload.keys;

import app.coronawarn.server.common.persistence.domain.FederationUploadKey;
import app.coronawarn.server.common.persistence.domain.validation.CountryValidator;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * This class is used to ensure that no key is uploaded to EFGS which does not allign with the
 * contraints imposed by EFGS (https://github.com/eu-federation-gateway-service/efgs-federation-gateway/issues/235).
 */
@Component
public class EfgsUploadKeyFilter {

  private static final Set<ReportType> EFGS_REPORT_TYPES = Set.of(ReportType.CONFIRMED_TEST);

  /**
   * Return true if the given key is compliant with EFGS upload constraints.
   */
  public boolean isUploadable(FederationUploadKey uploadKey) {
    return hasAcceptedReportTypes(uploadKey)
        && hasAcceptedDaysSinceSymptomsValue(uploadKey)
        && hasIsoCountryCodes(uploadKey);
  }

  private boolean hasIsoCountryCodes(FederationUploadKey uploadKey) {
    return CountryValidator.isValidCountryCodes(uploadKey.getVisitedCountries())
        && CountryValidator.isValidCountryCode(uploadKey.getOriginCountry());
  }

  private boolean hasAcceptedDaysSinceSymptomsValue(FederationUploadKey uploadKey) {
    // to be implemented when ranges are clear
    return true;
  }

  private boolean hasAcceptedReportTypes(FederationUploadKey uploadKey) {
    return EFGS_REPORT_TYPES.contains(uploadKey.getReportType());
  }
}
