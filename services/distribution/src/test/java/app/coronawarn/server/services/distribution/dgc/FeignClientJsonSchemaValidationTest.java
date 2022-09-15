package app.coronawarn.server.services.distribution.dgc;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import app.coronawarn.server.services.distribution.config.DistributionServiceConfig.AllowList;
import app.coronawarn.server.services.distribution.dgc.client.JsonSchemaDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import app.coronawarn.server.services.distribution.dgc.client.JsonSchemaMappingLookup;
import org.everit.json.schema.ValidationException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.google.common.reflect.TypeToken;

@ExtendWith(SpringExtension.class)
public class FeignClientJsonSchemaValidationTest {

  @Autowired
  ResourceLoader resourceLoader;

  private static final Type ALLOWLIST_OBJECT_TYPE = new TypeToken<AllowList>() {
  }.getType();

  private static final String RULE_LIST_REQUEST_ENDPOINT = "http://mydomain/rules";
  private static final String RULE_HASH_REQUEST_ENDPOINT = "http://mydomain/rules/DE/abcabc";
  private static final String BUSINESS_RULE_REQUEST_ENDPOINT = "http://mydomain/bnrules";
  private static final String BUSINESS_RULE_HASH_REQUEST_ENDPOINT = "http://mydomain/bnrules/abcabc";
  private static final String CCL_RULE_REQUEST_ENDPOINT = "http://mydomain/cclrules";
  private static final String CCL_RULE_HASH_REQUEST_ENDPOINT = "http://mydomain/cclrules/abcabc";

  @Test
  public void testBusinessRuleValidForSchema1() throws IOException {
    testJsonValidForSchema("dgc/ccl-configuration.json", "json-validation/rule.json");
  }

  @Test
  public void testBusinessRuleListValidForSchema1() throws IOException {
    testJsonValidForSchema("dgc/ccl-configuration.json", "/dgc/cclrules.json");
  }

  @Test
  public void testValueSetsValidForSchema1() throws IOException {
    testJsonValidForSchema("dgc/ccl-configuration.json", "/dgc/valuesets.json");
  }

  @Test
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

  @Test
  public void testBusinessRuleInvalidForSchema() throws IOException {
    InputStream schemaAsStream = resourceLoader.getResource("dgc/ccl-configuration.json").getInputStream();
    InputStream businessRuleJsonAsStream = resourceLoader.getResource(
        "dgc/ccl-configuration-sample_missing_required_property.json").getInputStream();
    JsonSchemaDecoder decoder = new JsonSchemaDecoder(null, null, resourceLoader);
    assertThatExceptionOfType(ValidationException.class).isThrownBy(
        () -> decoder.validateJsonAgainstSchema(businessRuleJsonAsStream, schemaAsStream));
  }

  @Test
  public void testBusinessRuleListEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals(null, lookup.getSchemaPath(BUSINESS_RULE_REQUEST_ENDPOINT));
  }

  @Test
  public void testBusinessRuleEndpointToSchemaMapping() {
    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
    Assert.assertEquals("dgc/ccl-configuration.json", lookup.getSchemaPath(BUSINESS_RULE_HASH_REQUEST_ENDPOINT));
  }

//  @Test
//  public void testAllowListJsonToSchemaMapping() {
//    JsonSchemaMappingLookup lookup = new JsonSchemaMappingLookup();
//    Assert.assertEquals("dgc/dcc-validation-service-allowlist-rule.json", lookup.getSchemaPath(ALLOWLIST_OBJECT_TYPE));
//  }
}
