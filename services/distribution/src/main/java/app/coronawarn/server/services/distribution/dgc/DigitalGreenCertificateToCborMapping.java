package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.*;

import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.everit.json.schema.ValidationException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DigitalGreenCertificateToCborMapping {

  public static final String DCC_VALIDATION_RULE_JSON_CLASSPATH = "dgc/dcc-validation-rule.json";
  private final DigitalCovidCertificateClient digitalCovidCertificateClient;

  private final ResourceLoader resourceLoader;

  public DigitalGreenCertificateToCborMapping(DigitalCovidCertificateClient digitalCovidCertificateClient,
      ResourceLoader resourceLoader) {
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
    this.resourceLoader = resourceLoader;
  }

  /**
   * Construct country rules retrieved from DCC client for CBOR encoding.
   *git s
   * @throws DigitalCovidCertificateException - exception thrown if anything happens while executing the logic. Example:
   *                                          countries could not be fetched from client. This exception will propagate
   *                                          and will stop any archive to be published down in the execution.
   */
  public List<String> constructCountryList() throws DigitalCovidCertificateException {
    return digitalCovidCertificateClient.getCountryList();
  }

  /**
   * Construct business rules retrieved from DCC client for CBOR encoding. Fetched rules are filtered by rule type
   * parameter which could be 'Acceptance' or 'Invalidation'.
   *
   * @param ruleType - rule type for which the business rules will be retrieved.
   * @return - business rules
   * @throws DigitalCovidCertificateException - exception thrown if anything happens while executing the logic.
   *                                          Examples: business rules could not be fetched, one specific rule could not
   *                                          be fetched, json validation fails for a specific rule etc. This exception
   *                                          will propagate and will stop any archive to be published down in the
   *                                          execution.
   */
  public List<BusinessRule> constructRules(RuleType ruleType) throws DigitalCovidCertificateException {
    List<BusinessRuleItem> businessRulesItems = digitalCovidCertificateClient.getRules();
    List<BusinessRule> businessRules = new ArrayList<>();

    for (BusinessRuleItem businessRuleItem : businessRulesItems) {
      Optional<BusinessRule> businessRuleOptional =
          digitalCovidCertificateClient.getCountryRuleByHash(
              businessRuleItem.getCountry(), businessRuleItem.getHash());

      if (businessRuleOptional.isPresent()) {
        BusinessRule businessRule = businessRuleOptional.get();

        if (businessRule.getType().equalsIgnoreCase(ruleType.name())) {
          try {
            validateJsonSchema(businessRule,
                resourceLoader.getResource(DCC_VALIDATION_RULE_JSON_CLASSPATH).getInputStream());
            businessRules.add(businessRule);
          } catch (JsonProcessingException | ValidationException e) {
            throw new DigitalCovidCertificateException(
                "Rule for country '" + businessRuleItem.getCountry() + "' having hash '" + businessRuleItem.getHash()
                + "' is not valid", e);
          } catch (IOException e) {
            throw new DigitalCovidCertificateException(
                "Validation rules schema found at: " + DCC_VALIDATION_RULE_JSON_CLASSPATH + "could not be found", e);
          }
        }
      } else {
        throw new DigitalCovidCertificateException("Rule for country '"
            + businessRuleItem.getCountry() + "' having hash '" + businessRuleItem.getHash()
            + "' could not be retrieved");
      }
    }

    return businessRules;
  }

  /**
   * CBOR encoding of {@code constructCountryList}.
   */
  public byte[] constructCborCountries() throws DigitalCovidCertificateException {
    return cborEncodeOrElseThrow(constructCountryList());
  }

  /**
   * CBOR encoding of {@code constructRules}.
   */
  public byte[] constructCborRules(RuleType ruleType) throws DigitalCovidCertificateException {
    return cborEncodeOrElseThrow(constructRules(ruleType));
  }

  private byte[] cborEncodeOrElseThrow(Object subject) throws DigitalCovidCertificateException {
    try {
      return cborEncode(subject);
    } catch (JsonProcessingException e) {
      throw new DigitalCovidCertificateException("Cbor encryption failed because of Json processing:", e);
    }
  }

}
