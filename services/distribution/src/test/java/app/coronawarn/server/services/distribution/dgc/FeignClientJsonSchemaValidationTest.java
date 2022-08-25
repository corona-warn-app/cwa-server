package app.coronawarn.server.services.distribution.dgc;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import app.coronawarn.server.services.distribution.dgc.client.JsonSchemaDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import org.everit.json.schema.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.google.common.reflect.TypeToken;

@ExtendWith(SpringExtension.class)
public class FeignClientJsonSchemaValidationTest {

  @Autowired
  ResourceLoader resourceLoader;

  //TOOD: Remove
  private static final Type BUSINESS_RULE_OBJECT_TYPE = new TypeToken<BusinessRule>() {
  }.getType();

  @Test
  public void testBusinessRuleValidForSchema() throws IOException {
    InputStream schemaAsStream = resourceLoader.getResource("dgc/ccl-configuration.json").getInputStream();
    InputStream businessRuleJsonAsStream = resourceLoader.getResource("dgc/ccl-configuration-sample.json")
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
}
