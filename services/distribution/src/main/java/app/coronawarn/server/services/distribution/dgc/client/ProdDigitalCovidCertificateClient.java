
package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateSignatureException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import app.coronawarn.server.services.distribution.dgc.exception.ThirdPartyServiceException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * This is an implementation with test data for interface retrieving Digital Covid Certificate data. Used to make HTTP
 * request to Digital Covid Certificate server.
 */
@Component
@Profile("!fake-dcc-client")
public class ProdDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDigitalCovidCertificateClient.class);

  private final DigitalCovidCertificateFeignClient digitalCovidCertificateClient;

  private final DccSignatureValidator dccSignatureValidator;

  public ProdDigitalCovidCertificateClient(DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient,
      DccSignatureValidator dccSignatureValidator) {
    this.digitalCovidCertificateClient = digitalCovidCertificateFeignClient;
    this.dccSignatureValidator = dccSignatureValidator;
  }

  @Override
  public List<String> getCountryList() throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(digitalCovidCertificateClient::getCountryList,
        "country list",
        FetchBusinessRulesException::new);
  }

  @Override
  public List<ValueSetMetadata> getValueSets() throws FetchValueSetsException {
    return getResponseAndTreatExceptions(digitalCovidCertificateClient::getValueSets,
        "value sets",
        FetchValueSetsException::new);
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) throws FetchValueSetsException {
    return Optional.ofNullable(
        getResponseAndTreatExceptions(
            () -> digitalCovidCertificateClient.getValueSet(hash),
        "value set",
        FetchValueSetsException::new)
    );
  }

  @Override
  public List<BusinessRuleItem> getRules() throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(digitalCovidCertificateClient::getRules,
        "business rules",
        FetchBusinessRulesException::new);
  }

  @Override
  public Optional<BusinessRule> getCountryRuleByHash(String country, String hash) throws FetchBusinessRulesException {
    return Optional.ofNullable(
        getResponseAndTreatExceptions(
            () -> digitalCovidCertificateClient.getCountryRule(country, hash),
            "country rule",
            FetchBusinessRulesException::new)
    );
  }

  private <T,E extends ThirdPartyServiceException> T getResponseAndTreatExceptions(
      Supplier<ResponseEntity<T>> responseSupplier,
      String fetchEntityName,
      BiFunction<String, Exception, E> exceptionConverter)
      throws E {
    logger.debug("Get " + fetchEntityName + " from DCC");

    try {
      ResponseEntity<T> response = responseSupplier.get();
      logger.debug("DCC " + fetchEntityName + " retrieved. Start signature verification.");
      dccSignatureValidator.checkSignature(response);

      return response.getBody();
    } catch (DigitalCovidCertificateSignatureException e) {
      logger.error("Signature verification failed for DCC " +  fetchEntityName  + ":", e);
      throw exceptionConverter.apply("Signature verification failed for " +  fetchEntityName, e);
    } catch (Exception e) {
      throw exceptionConverter.apply(fetchEntityName + " could not be fetched because of: ", e);
    }
  }
}
