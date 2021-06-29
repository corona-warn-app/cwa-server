package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.dgc.BusinessRule.RuleType;
import app.coronawarn.server.services.distribution.dgc.client.DigitalCovidCertificateClient;
import app.coronawarn.server.services.distribution.dgc.exception.DigitalCovidCertificateException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class DigitalGreenCertificateToCborMapping {

  private static final Logger logger = LoggerFactory.getLogger(DigitalGreenCertificateToCborMapping.class);

  private DigitalCovidCertificateClient digitalCovidCertificateClient;

  @Autowired
  ResourceLoader resourceLoader;

  public DigitalGreenCertificateToCborMapping(DigitalCovidCertificateClient digitalCovidCertificateClient) {
    this.digitalCovidCertificateClient = digitalCovidCertificateClient;
  }

  /**
   * TODO: write javadoc.
   */
  public byte[] constructCountryList() throws DigitalCovidCertificateException {
    ObjectMapper cborMapper = new CBORMapper();
    try {
      return cborMapper.writeValueAsBytes(digitalCovidCertificateClient.getCountryList());
    } catch (JsonProcessingException e) {
      throw new DigitalCovidCertificateException("Cbor encryption failed because of Json processing:", e);
    }
  }

  /**
   * TODO: write javadoc.
   */
  public List<BusinessRule> constructRules(RuleType ruleType) throws DigitalCovidCertificateException {
    List<BusinessRuleItem> businessRulesItems = digitalCovidCertificateClient.getRules();
    List<BusinessRule> businessRules = new ArrayList<>();

    for (BusinessRuleItem businessRuleItem : businessRulesItems) {
      Optional<BusinessRule> businessRuleOptional =
          digitalCovidCertificateClient.getCountryRuleByHash(
              businessRuleItem.getCountryCode(), businessRuleItem.getHash());

      if (businessRuleOptional.isPresent()) {
        BusinessRule businessRule = businessRuleOptional.get();

        if (businessRule.getType().equalsIgnoreCase(ruleType.name())) {
          try {
            validateJsonSchema(businessRule);
            businessRules.add(businessRule);
          } catch (ValidationException e) {
            logger.error("Business rule with identifier: " + businessRule.getIdentifier()
                + " did not pass validation schema");
          }
          businessRules.add(businessRule);
        }
      }
    }

    return businessRules;
  }

  public byte[] constructCborCountries(RuleType ruleType) throws DigitalCovidCertificateException {
    return cborEncode(constructCountryList());
  }

  public byte[] constructCborRules(RuleType ruleType) throws DigitalCovidCertificateException {
    return cborEncode(constructRules(ruleType));
  }

  private byte[] cborEncode(Object object) throws DigitalCovidCertificateException {
    ObjectMapper cborMapper = new CBORMapper();
    try {
      return cborMapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      throw new DigitalCovidCertificateException("Cbor encryption failed because of Json processing:", e);
    }
  }

  private void validateJsonSchema(BusinessRule businessRule) throws DigitalCovidCertificateException {
    JSONObject jsonSchema = null;
    ObjectMapper objectMapper = new ObjectMapper();


    try {
      jsonSchema = new JSONObject(
          new JSONTokener(resourceLoader.getResource("dgc/dcc-validation-rule.json").getInputStream()));
      String businessRuleJson = objectMapper.writeValueAsString(businessRule);
      JSONObject jsonSubject = new JSONObject(businessRuleJson);

      Schema schema = SchemaLoader.load(jsonSchema);
      schema.validate(jsonSubject);
    } catch (IOException e) {
      throw new DigitalCovidCertificateException("Error occured on loading DCC validation rules", e);
    }


  }


}
