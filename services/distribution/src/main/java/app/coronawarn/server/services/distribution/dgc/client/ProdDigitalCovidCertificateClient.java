
package app.coronawarn.server.services.distribution.dgc.client;

import app.coronawarn.server.services.distribution.dgc.BusinessRule;
import app.coronawarn.server.services.distribution.dgc.BusinessRuleItem;
import app.coronawarn.server.services.distribution.dgc.ValueSet;
import app.coronawarn.server.services.distribution.dgc.ValueSetMetadata;
import app.coronawarn.server.services.distribution.dgc.client.signature.DccSignatureValidator;
import app.coronawarn.server.services.distribution.dgc.client.signature.VerifyDccSignature;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateSignatureException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchValueSetsException;
import java.util.List;
import java.util.Optional;
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

  private DigitalCovidCertificateFeignClient digitalCovidCertificateClient;

  private DccSignatureValidator dccSignatureValidator;

  public ProdDigitalCovidCertificateClient(DigitalCovidCertificateFeignClient digitalCovidCertificateFeignClient,
      DccSignatureValidator dccSignatureValidator) {
    this.digitalCovidCertificateClient = digitalCovidCertificateFeignClient;
    this.dccSignatureValidator = dccSignatureValidator;
  }

  @Override
  @VerifyDccSignature
  public List<String> getCountryList() throws FetchBusinessRulesException {
    logger.debug("Get country list from DCC");
    try {
      ResponseEntity<List<String>> response = digitalCovidCertificateClient.getCountryList();
      logger.debug("DCC country list retrieved. Start signature verification.");
      dccSignatureValidator.checkSignature(response);

      return response.getBody();
    } catch (DigitalCovidCertificateSignatureException e) {
      logger.error("Signature verification failed for DCC country list:", e);
      throw new FetchBusinessRulesException("Signature verification failed for country list: ", e);
    } catch (Exception e) {
      throw new FetchBusinessRulesException("Business rules could not be fetched because of: ", e);
    }
  }

  @Override
  public List<ValueSetMetadata> getValueSets() throws FetchValueSetsException {
    logger.debug("Get valuesets from DCC");
    try {
      ResponseEntity<List<ValueSetMetadata>> response = digitalCovidCertificateClient.getValueSets();
      logger.debug("DCC value sets retrieved. Start signature verification.");
      dccSignatureValidator.checkSignature(response);

      return response.getBody();
    } catch (DigitalCovidCertificateSignatureException e) {
      logger.error("Signature verification failed for DCC valuesets:", e);
      throw new FetchValueSetsException("Signature verification failed for valuesets: ", e);
    } catch (Exception e) {
      throw new FetchValueSetsException("Value sets could not be fetched because of: ", e);
    }
  }

  @Override
  public Optional<ValueSet> getValueSet(String hash) throws FetchValueSetsException {
    logger.debug("Get valuesets having hash: " + hash + " from DCC");
    try {
      ResponseEntity<ValueSet> response = digitalCovidCertificateClient.getValueSet(hash);
      logger.debug("DCC value set retrieved. Start signature verification.");
      dccSignatureValidator.checkSignature(response);

      return Optional.ofNullable(response.getBody());
    } catch (DigitalCovidCertificateSignatureException e) {
      logger.error("Signature verification failed for DCC value set:", e);
      throw new FetchValueSetsException("Signature verification failed for value set: ", e);
    } catch (Exception e) {
      throw new FetchValueSetsException("Value set with hash '" + hash
          + "' could not be fetched because of: ", e);
    }

  }

  @Override
  public List<BusinessRuleItem> getRules() throws FetchBusinessRulesException {
    logger.debug("Get rules from DCC");
    try {
      ResponseEntity<List<BusinessRuleItem>> response = digitalCovidCertificateClient.getRules();
      logger.debug("DCC business rules retrieved. Start signature verification.");
      dccSignatureValidator.checkSignature(response);

      return response.getBody();
    } catch (DigitalCovidCertificateSignatureException e) {
      logger.error("Signature verification failed for DCC business rules:", e);
      throw new FetchBusinessRulesException("Signature verification failed for business rules: ", e);
    } catch (Exception e) {
      throw new FetchBusinessRulesException("Business rules could not be fetched because of: ", e);
    }
  }

  @Override
  public Optional<BusinessRule> getCountryRuleByHash(String country, String hash) throws FetchBusinessRulesException {
    logger.debug("Get business rule having country:" + country + " and hash: " + hash + "from DCC");
    try {
      ResponseEntity<BusinessRule> response = digitalCovidCertificateClient.getCountryRule(country, hash);
      logger.debug("DCC business rule retrieved. Start signature verification.");
      dccSignatureValidator.checkSignature(response);

      return Optional.ofNullable(response.getBody());
    } catch (DigitalCovidCertificateSignatureException e) {
      logger.error("Signature verification failed for DCC business rule:", e);
      throw new FetchBusinessRulesException("Signature verification failed for business rule: ", e);
    } catch (Exception e) {
      throw new FetchBusinessRulesException("Business rules with country '" + country + "' and hash '"
          + hash + "' could not be fetched because of: ", e);
    }
  }
}
