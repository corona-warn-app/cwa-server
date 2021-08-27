package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.common.shared.util.SerializationUtils.cborEncode;
import static app.coronawarn.server.common.shared.util.SerializationUtils.validateJsonSchema;

import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import app.coronawarn.server.services.distribution.dgc.exception.FetchBusinessRulesException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
   */
  public List<String> constructCountryList() throws FetchBusinessRulesException {
    return digitalCovidCertificateClient.getCountryList();
  }

  /**
   * Construct business rules retrieved from DCC client for CBOR encoding. Fetched rules are filtered by rule type
   * parameter which could be 'Acceptance' or 'Invalidation'.
   *
   * @param ruleType - rule type for which the business rules will be retrieved.
   * @return - business rules
   * @throws DigitalCovidCertificateException - thrown if json validation schema is not found or the validation fails
   *                                          for a specific rule. This exception will propagate and will stop any
   *                                          archive to be published down in the execution.
   */
  public List<BusinessRule> constructRules(RuleType ruleType)
      throws DigitalCovidCertificateException, FetchBusinessRulesException {
    List<BusinessRuleItem> businessRulesItems = digitalCovidCertificateClient.getRules();
    List<BusinessRule> businessRules = new ArrayList<>();

    for (BusinessRuleItem businessRuleItem : businessRulesItems) {
      BusinessRule businessRule =
          digitalCovidCertificateClient.getCountryRuleByHash(
              businessRuleItem.getCountry(), businessRuleItem.getHash());

      if (businessRule.getType().equalsIgnoreCase(ruleType.name())) {
        try (final InputStream in = resourceLoader.getResource(DCC_VALIDATION_RULE_JSON_CLASSPATH).getInputStream()) {
          validateJsonSchema(businessRule, in);
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
    }

    return businessRules;
  }

  /**
   * Construct business rules retrieved from DCC client for CBOR encoding. Fetched rules are filtered by rule type
   * parameter which could be 'BoosterNotification'.
   *
   * @param ruleType - rule type for which the business rules will be retrieved.
   * @return - business rules
   * @throws DigitalCovidCertificateException - thrown if json validation schema is not found or the validation fails
   *                                          for a specific rule. This exception will propagate and will stop any
   *                                          archive to be published down in the execution.
   */
  public List<BusinessRule> constructBnRules(RuleType ruleType)
      throws DigitalCovidCertificateException, FetchBusinessRulesException {
    List<BusinessRuleItem> businessRulesItems = digitalCovidCertificateClient.getBnRules();
    List<BusinessRule> businessRules = new ArrayList<>();

    for (BusinessRuleItem businessRuleItem : businessRulesItems) {
      BusinessRule businessRule =
          digitalCovidCertificateClient.getCountryRuleByHash(
              businessRuleItem.getCountry(), businessRuleItem.getHash());

      if (businessRule.getType().equalsIgnoreCase(ruleType.name())) {
        try (final InputStream in = resourceLoader.getResource(DCC_VALIDATION_RULE_JSON_CLASSPATH).getInputStream()) {
          validateJsonSchema(businessRule, in);
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
    }

    return businessRules;
  }

  /**
   * CBOR encoding of {@code constructCountryList}.
   */
  public byte[] constructCborCountries() throws DigitalCovidCertificateException,
      FetchBusinessRulesException {
    return cborEncodeOrElseThrow(constructCountryList());
  }

  /**
   * CBOR encoding of {@code constructRules}.
   */
  public byte[] constructCborRules(RuleType ruleType)
      throws DigitalCovidCertificateException, FetchBusinessRulesException {
    return cborEncodeOrElseThrow(constructRules(ruleType));
  }

  /**
   * CBOR encoding of {@code constructBnRules}.
   */
  public byte[] constructCborBnRules(RuleType ruleType)
      throws DigitalCovidCertificateException, FetchBusinessRulesException {
    return cborEncodeOrElseThrow(constructBnRules(ruleType));
  }


  private byte[] cborEncodeOrElseThrow(Object subject) throws DigitalCovidCertificateException {
    try {
      return cborEncode(subject);
    } catch (JsonProcessingException e) {
      throw new DigitalCovidCertificateException("Cbor encryption failed because of Json processing:", e);
    }
  }

}
