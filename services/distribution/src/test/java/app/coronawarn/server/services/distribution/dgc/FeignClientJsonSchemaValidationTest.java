package app.coronawarn.server.services.distribution.dgc;

import app.coronawarn.server.services.distribution.dgc.client.JsonSchemaDecoder;
import app.coronawarn.server.services.distribution.dgc.client.JsonSchemaMappingLookup;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
public class FeignClientJsonSchemaValidationTest {

  private static final String RULE_LIST_REQUEST_ENDPOINT = "http://mydomain/rules";
  private static final String RULE_HASH_REQUEST_ENDPOINT = "http://mydomain/rules/DE/abcabc";
  private static final String BOOSTER_NOTIFICATION_RULE_LIST_REQUEST_ENDPOINT = "http://mydomain/bnrules";
  private static final String BOOSTER_NOTIFICATION_HASH_REQUEST_ENDPOINT = "http://mydomain/bnrules/abcabc";
  private static final String CCL_RULE_LIST_REQUEST_ENDPOINT = "http://mydomain/cclrules";
  private static final String CCL_RULE_HASH_REQUEST_ENDPOINT = "http://mydomain/cclrules/abcabc";
  @Autowired
  ResourceLoader resourceLoader;

  @Test
  public void testBusinessRuleValidForSchema() throws IOException {
    testJsonValidForSchema("dgc/dcc-validation-rule.json", "dgc/json-validation/rule.json");
  }

  @Test
  public void testBusinessRuleInvalidForSchema() throws IOException {
    testJsonInvalidForSchema("dgc/dcc-validation-rule.json",
        "dgc/json-validation/rule_invalid.json");
  }

  @Test
  public void testBoosterNotificationValidForSchema() throws IOException {
    testJsonValidForSchema("dgc/dcc-validation-rule.json", "dgc/json-validation/bnrule.json");
  }

  @Test
  public void testBoosterNotificationInvalidForSchema() throws IOException {
    testJsonInvalidForSchema("dgc/dcc-validation-rule.json", "dgc/json-validation/bnrule_invalid.json");
  }

  @Test
  public void testCclRuleValidForSchema() throws IOException {
    testJsonValidForSchema("dgc/ccl-configuration.json", "dgc/json-validation/ccl-configuration.json");
  }

  @Test
  public void testCclRuleInvalidForSchema() throws IOException {
    testJsonInvalidForSchema("dgc/ccl-configuration.json", "dgc/json-validation/ccl-configuration_invalid.json");
  }

  public void testJsonValidForSchema(String schemaLocation, String jsonPayloadLocation) throws IOException {
    InputStream schemaAsStream = resourceLoader.getResource(schemaLocation).getInputStream();
    InputStream businessRuleJsonAsStream = resourceLoader.getResource(jsonPayloadLocation)
        .getInputStream();
    JsonSchemaDecoder decoder = new JsonSchemaDecoder(null, null, resourceLoader);
    try {
      decoder.validateJsonAgainstSchema(businessRuleJsonAsStream, schemaAsStream);
    } catch (ValidationException ex) {
      fail("Json should have been correctly verified by given schema", ex);
    }
  }

  public void testJsonInvalidForSchema(String schemaLocation, String jsonPayloadLocation) throws IOException {
    InputStream schemaAsStream = resourceLoader.getResource(schemaLocation).getInputStream();
    InputStream businessRuleJsonAsStream = resourceLoader.getResource(jsonPayloadLocation)
        .getInputStream();
    JsonSchemaDecoder decoder = new JsonSchemaDecoder(null, null, resourceLoader);
    assertThatExceptionOfType(ValidationException.class).isThrownBy(
        () -> decoder.validateJsonAgainstSchema(businessRuleJsonAsStream, schemaAsStream));
  }

  @Test
  public void testRuleListEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(null, lookup.getSchemaPath(RULE_LIST_REQUEST_ENDPOINT));
  }

  @Test
  public void testRuleEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals("dgc/ccl-configuration.json", lookup.getSchemaPath(RULE_HASH_REQUEST_ENDPOINT));
  }

  @Test
  public void testBoosterNotificationListEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(null, lookup.getSchemaPath(BOOSTER_NOTIFICATION_RULE_LIST_REQUEST_ENDPOINT));
  }

  @Test
  public void testBoosterNotificationEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals("dgc/dcc-validation-rule.json",
        lookup.getSchemaPath(BOOSTER_NOTIFICATION_HASH_REQUEST_ENDPOINT));
  }

  @Test
  public void testCclRuleListEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(null, lookup.getSchemaPath(CCL_RULE_LIST_REQUEST_ENDPOINT));
  }

  @Test
  public void testCclRuleEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals("dgc/ccl-configuration.json", lookup.getSchemaPath(CCL_RULE_HASH_REQUEST_ENDPOINT));
  }

//  @Test
//  public void testAllowListJsonToSchemaMapping() {
//    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
//    Assert.assertEquals("dgc/dcc-validation-service-allowlist-rule.json", lookup.getSchemaPath(ALLOWLIST_OBJECT_TYPE));
//  }
}
