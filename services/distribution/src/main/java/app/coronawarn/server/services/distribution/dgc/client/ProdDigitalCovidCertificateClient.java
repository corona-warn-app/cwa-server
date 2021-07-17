
package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This is an implementation with test data for interface retrieving Digital Covid Certificate data. Used to make HTTP
 * request to Digital Covid Certificate server.
 */
@Component
@Profile("!fake-dcc-client")
public class ProdDigitalCovidCertificateClient implements DigitalCovidCertificateClient {

  private static final Logger logger = LoggerFactory.getLogger(ProdDigitalCovidCertificateClient.class);

  private DigitalCovidCertificateFeignClient digitalCovidCertificateClient;

  public ProdDigitalCovidCertificateClient(DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateFeignClient;
  }

  @Override
  public List<String> getCountryList() throws FetchBusinessRulesException {
    logger.debug("Get country list from DCC");
    try {
      return digitalCovidCertificateClient.getCountryList().getBody();
    } catch (Exception e) {
      throw new FetchBusinessRulesException("Business rules could not be fetched because of: ", e);
    }
  }

  @Override
  public List<ValueSetMetadata> getValueSets() throws FetchValueSetsException {
    logger.debug("Get valuesets from DCC");
    try {
      return digitalCovidCertificateClient.getValueSets().getBody();
    } catch (Exception e) {
      throw new FetchValueSetsException("Value sets could not be fetched because of: ", e);
    }
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) throws FetchValueSetsException {
    logger.debug("Get valuesets having hash: " + hash + " from DCC");
    try {
      return Optional.ofNullable(digitalCovidCertificateClient.getValueSet(hash).getBody());
    } catch (Exception e) {
      throw new FetchValueSetsException("Value set with hash '" + hash
          + "' could not be fetched because of: ", e);
    }

  }

  @Override
  public List<BusinessRuleItem> getRules() throws FetchBusinessRulesException {
    logger.debug("Get rules from DCC");
    try {
      return digitalCovidCertificateClient.getRules().getBody();
    } catch (Exception e) {
      throw new FetchBusinessRulesException("Business rules could not be fetched because of: ", e);
    }
  }

  @Override
  public Optional<BusinessRule> getCountryRuleByHash(String country, String hash) throws FetchBusinessRulesException {
    logger.debug("Get business rule having country:" + country + " and hash: " + hash + "from DCC");
    try {
      return Optional.ofNullable(digitalCovidCertificateClient.getCountryRule(country, hash).getBody());
    } catch (Exception e) {
      throw new FetchBusinessRulesException("Business rules with country '" + country + "' and hash '"
          + hash + "' could not be fetched because of: ", e);
    }
  }

}
