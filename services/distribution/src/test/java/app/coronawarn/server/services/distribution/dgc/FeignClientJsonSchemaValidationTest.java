package app.coronawarn.server.services.distribution.dgc;

import static app.coronawarn.server.services.distribution.assembly.component.CommonCovidLogicArchiveBuilder.CCL_JSON_SCHEMA;
import static app.coronawarn.server.services.distribution.dgc.DigitalGreenCertificateToCborMapping.DCC_VALIDATION_RULE_JSON_CLASSPATH;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import app.coronawarn.server.services.distribution.dgc.client.JsonSchemaMappingLookup;
import app.coronawarn.server.services.distribution.dgc.client.JsonValidationService;
import java.io.IOException;
import java.io.InputStream;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JsonValidationService.class
},
    initializers = ConfigDataApplicationContextInitializer.class)
class FeignClientJsonSchemaValidationTest {

  private static final String COUNTRY_RULE_LIST_REQUEST_ENDPOINT = "http://mydomain/rules";
  private static final String COUNTRY_RULE_HASH_REQUEST_ENDPOINT = "http://mydomain/rules/DE/abcabc";
  private static final String BOOSTER_NOTIFICATION_RULE_LIST_REQUEST_ENDPOINT = "http://mydomain/bnrules";
  private static final String BOOSTER_NOTIFICATION_HASH_REQUEST_ENDPOINT = "http://mydomain/bnrules/abcabc";
  private static final String CCL_RULE_LIST_REQUEST_ENDPOINT = "http://mydomain/cclrules";
  private static final String CCL_RULE_HASH_REQUEST_ENDPOINT = "http://mydomain/cclrules/abcabc";

  @Autowired
  ResourceLoader resourceLoader;

  @Autowired
  JsonValidationService jsonValidationService;

  @Test
  void testCountryBusinessRuleValidForSchema() throws IOException {
    testJsonValidForSchema(DCC_VALIDATION_RULE_JSON_CLASSPATH, "dgc/json-validation/rule.json");
  }

  @Test
  void testCountryBusinessRuleInvalidForSchema() throws IOException {
    testJsonInvalidForSchema(DCC_VALIDATION_RULE_JSON_CLASSPATH,
        "dgc/json-validation/rule_invalid.json");
  }

  @Test
  void testBoosterNotificationValidForSchema() throws IOException {
    testJsonValidForSchema(DCC_VALIDATION_RULE_JSON_CLASSPATH, "dgc/json-validation/bnrule.json");
  }

  @Test
  void testBoosterNotificationInvalidForSchema() throws IOException {
    testJsonInvalidForSchema(DCC_VALIDATION_RULE_JSON_CLASSPATH, "dgc/json-validation/bnrule_invalid.json");
  }

  @Test
  void testCclRuleValidForSchema() throws IOException {
    testJsonValidForSchema(CCL_JSON_SCHEMA, "dgc/json-validation/ccl-configuration.json");
  }

  @Test
  void testCclRuleInvalidForSchema() throws IOException {
    testJsonInvalidForSchema(CCL_JSON_SCHEMA, "dgc/json-validation/ccl-configuration_invalid.json");
  }

  public void testJsonValidForSchema(String schemaLocation, String jsonPayloadLocation) throws IOException {
    InputStream schemaAsStream = resourceLoader.getResource(schemaLocation).getInputStream();
    InputStream businessRuleJsonAsStream = resourceLoader.getResource(jsonPayloadLocation)
        .getInputStream();
    try {
      jsonValidationService.validateJsonAgainstSchema(businessRuleJsonAsStream, schemaAsStream);
    } catch (ValidationException ex) {
      fail("Json should have been correctly verified by given schema", ex);
    }
  }

  public void testJsonInvalidForSchema(String schemaLocation, String jsonPayloadLocation) throws IOException {
    InputStream schemaAsStream = resourceLoader.getResource(schemaLocation).getInputStream();
    InputStream businessRuleJsonAsStream = resourceLoader.getResource(jsonPayloadLocation)
        .getInputStream();
    assertThatExceptionOfType(ValidationException.class).isThrownBy(
        () -> jsonValidationService.validateJsonAgainstSchema(businessRuleJsonAsStream, schemaAsStream));
  }

  @Test
  void testRuleListEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(null, lookup.getSchemaPath(COUNTRY_RULE_LIST_REQUEST_ENDPOINT));
  }

  @Test
  void testRuleEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(DCC_VALIDATION_RULE_JSON_CLASSPATH, lookup.getSchemaPath(COUNTRY_RULE_HASH_REQUEST_ENDPOINT));
  }

  @Test
  void testBoosterNotificationListEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(null, lookup.getSchemaPath(BOOSTER_NOTIFICATION_RULE_LIST_REQUEST_ENDPOINT));
  }

  @Test
  void testBoosterNotificationEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(DCC_VALIDATION_RULE_JSON_CLASSPATH,
        lookup.getSchemaPath(BOOSTER_NOTIFICATION_HASH_REQUEST_ENDPOINT));
  }

  @Test
  void testCclRuleListEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(null, lookup.getSchemaPath(CCL_RULE_LIST_REQUEST_ENDPOINT));
  }

  @Test
  void testCclRuleEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(CCL_JSON_SCHEMA, lookup.getSchemaPath(CCL_RULE_HASH_REQUEST_ENDPOINT));
  }
}
