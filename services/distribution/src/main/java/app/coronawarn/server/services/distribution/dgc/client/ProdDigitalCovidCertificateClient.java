package app.coronawarn.server.services.distribution.dgc.client;

import static app.coronawarn.server.common.shared.util.SerializationUtils.stringifyObject;
import static java.util.function.Predicate.not;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import app.coronawarn.server.services.distribution.dgc.exception.ThirdPartyServiceException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * This is an implementation with test data for interface retrieving Digital Covid Certificate data. Used to make HTTP
 * request to Digital Covid Certificate server.
 */
@Component
@Profile("!fake-dcc-client & !revocation")
public class ProdDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDigitalCovidCertificateClient.class);

  public static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

  private final DigitalCovidCertificateFeignClient digitalCovidCertificateClient;

  public ProdDigitalCovidCertificateClient(DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateFeignClient;
  }

  @Override
  public List<String> getCountryList() throws FetchBusinessRulesException {
    try {
      ResponseEntity<List<String>> responseEntity = digitalCovidCertificateClient.getCountryList();
      List<String> countryList = responseEntity.getBody();
      final List<String> countryListResponse;
      if (countryList != null) {
        countryListResponse = countryList.stream().filter(not("EU"::equals)).collect(Collectors.toList());
        //countryList.removeIf("EU"::equals); //remove might not be supported
      } else {
        countryListResponse = null;
      }
      return getResponseAndTreatExceptions(() -> ResponseEntity.ok(countryListResponse),
          "country list",
          FetchBusinessRulesException::new);
    } catch (Exception e) {
      throw new FetchBusinessRulesException(e.getMessage());
    }
  }

  @Override
  public List<ValueSetMetadata> getValueSets() throws FetchValueSetsException {
    return getResponseAndTreatExceptions(digitalCovidCertificateClient::getValueSets,
        "value sets",
        FetchValueSetsException::new);
  }

  @Override
  public ValueSet getValueSet(String hash) throws FetchValueSetsException {
    return getResponseAndTreatExceptions(
        () -> digitalCovidCertificateClient.getValueSet(hash),
        "value set",
        FetchValueSetsException::new);
  }

  @Override
  public List<BusinessRuleItem> getRules() throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(digitalCovidCertificateClient::getRules,
        "business rules",
        FetchBusinessRulesException::new);
  }

  @Override
  public List<BusinessRuleItem> getBoosterNotificationRules() throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(digitalCovidCertificateClient::getBoosterNotificationRules,
        "booster notification business rules",
        FetchBusinessRulesException::new);
  }

  @Override
  public List<BusinessRuleItem> getCommonCovidLogicRules() throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(digitalCovidCertificateClient::getCommonCovidLogicRules,
        "common covid logic rules",
        FetchBusinessRulesException::new);
  }

  @Override
  public BusinessRule getCountryRuleByHash(String country, String hash) throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(
        () -> digitalCovidCertificateClient.getCountryRule(country, hash),
        "country rule",
        FetchBusinessRulesException::new);
  }

  @Override
  public BusinessRule getBoosterNotificationRuleByHash(String country, String hash) throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(
        () -> digitalCovidCertificateClient.getBoosterNotificationRule(hash),
        "bn rule",
        FetchBusinessRulesException::new);
  }

  @Override
  public BusinessRule getCommonCovidLogicRuleByHash(String country, String hash) throws FetchBusinessRulesException {
    return getResponseAndTreatExceptions(
        () -> digitalCovidCertificateClient.getCommonCovidLogicRule(hash),
        "ccl rule",
        FetchBusinessRulesException::new);
  }

  private <T, E extends ThirdPartyServiceException> T getResponseAndTreatExceptions(
      Supplier<ResponseEntity<T>> responseSupplier,
      String fetchEntityName,
      BiFunction<String, Exception, E> exceptionConverter)
      throws E {
    logger.debug("Get {} from DCC", fetchEntityName);
    try {
      ResponseEntity<T> response = responseSupplier.get();

      if (response.getBody() == null) {
        throw exceptionConverter.apply("Response body for " + fetchEntityName + " is null",
            new NullPointerException());
      }

      logger.info(AUDIT, "{} - {}", fetchEntityName, stringifyObject(response.getBody()));
      return response.getBody();
    } catch (Exception e) {
      throw exceptionConverter.apply(fetchEntityName + " could not be fetched because of: " + e.getMessage(), e);
    }
  }

}
