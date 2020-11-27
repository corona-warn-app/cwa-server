package app.coronawarn.server.services.federation.upload.keys;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import app.coronawarn.server.common.persistence.domain.FederationUploadKey;

/**
 *  The Federation Gateway to which keys are uploaded enforces several rules regarding key validity.
 *  Some of the paramters in the CWA persisted keys can be adapted to fit the EFGS rules without breaking
 *  key information consistency. This component ensures keys are adapted to the EFGS requirements.
 */
@Component
public class EfgsKeyParameterAdapter {

  /**
   * Given the collection of keys, create another one with all original elements but with parameters
   * transformed to allign with EFGS constraints (where applicable).
   */
  public List<FederationUploadKey> adaptToEfgsRequirements(List<FederationUploadKey> pendingUploadKeys) {
    return pendingUploadKeys.stream().map(this::adapt).collect(Collectors.toList());
  }

  private FederationUploadKey adapt(FederationUploadKey uploadableKey) {
    return FederationUploadKey.from(uploadableKey, adaptVisitedCountries(uploadableKey));
  }

  private Set<String> adaptVisitedCountries(FederationUploadKey uploadableKey) {
    Set<String> visitedCountries = uploadableKey.getVisitedCountries();
    String originCountry = uploadableKey.getOriginCountry();
    visitedCountries.removeIf( country -> country.trim().equalsIgnoreCase(originCountry.trim()));
    return visitedCountries;
  }
}
