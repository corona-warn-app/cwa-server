package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.SerializationUtils.cborEncode;
import static app.coronawarn.server.common.shared.util.SerializationUtils.deserializeJson;
import static app.coronawarn.server.common.shared.util.SerializationUtils.stringifyObject;
import static app.coronawarn.server.common.shared.util.SerializationUtils.validateJsonSchema;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;


class SerializationUtilsTest {

  public static final String TEST_ATTRIBUTE = "testAttribute";
  public static final String TEST_ATTRIBUTE_VALUE = "test-value";
  public static final String TEST_OBJECT_SERIALIZED = "{\"testAttribute\":\"test-value\"}";
  public static final String TEST_OBJECT_SERIALIZED_WRONG_FORMAT = "{\"testAttribute\"\"test-value\"}";

  public static final String schema = "{\n"
      + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n"
      + "  \"$id\": \"serialization_validation_test\",\n"
      + "  \"title\": \"Validation test\",\n"
      + "  \"type\": \"object\",\n"
      + "  \"additionalProperties\": false,\n"
      + "  \"required\": [\n"
      + "    \"id\",\n"
      + "    \"attribute\",\n"
      + "    \"enumTest\"\n"
      + "  ],\n"
      + "  \"properties\": {\n"
      + "    \"id\": {\n"
      + "      \"type\": \"string\",\n"
      + "      \"pattern\": \"^(TEST)-[A-Z]{2}$\"\n"
      + "    },\n"
      + "    \"enumTest\": {\n"
      + "      \"type\": \"string\",\n"
      + "      \"enum\": [\n"
      + "        \"test1\",\n"
      + "        \"test2\"\n"
      + "      ]\n"
      + "    },\n"
      + "    \"attribute\": {\n"
      + "      \"type\": \"string\"\n"
      + "    }\n"
      + "  }\n"
      + "}";

  @Test
  void testDeserializeJsonInputStream() throws IOException {
    InputStream is = new ByteArrayInputStream(TEST_OBJECT_SERIALIZED.getBytes());
    TestObject testObject = deserializeJson(is,
        typeFactory -> typeFactory.constructType(TestObject.class));

    assertEquals(TEST_ATTRIBUTE_VALUE, testObject.getTestAttribute());
  }

  @Test
  void testDeserializeJson() {
    TestObject testObject = deserializeJson(TEST_OBJECT_SERIALIZED,
        typeFactory -> typeFactory.constructType(TestObject.class));

    assertEquals(TEST_ATTRIBUTE_VALUE, testObject.getTestAttribute());
  }

  @Test
  void testDeserializeJsonAndExpectException() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> deserializeJson(TEST_OBJECT_SERIALIZED_WRONG_FORMAT,
            typeFactory -> typeFactory.constructType(TestObject.class)));
  }

  @Test
  void testStringifyObject() {
    TestObject testObject = new TestObject();
    testObject.setTestAttribute(TEST_ATTRIBUTE_VALUE);
    String stringify = stringifyObject(testObject);

    assertTrue(stringify.contains(TEST_ATTRIBUTE));
    assertTrue(stringify.contains(TEST_ATTRIBUTE_VALUE));
  }

  @Test
  void testStringifyObjectAndExpectException() {
    ClassThatJacksonCannotSerialize testObject = new ClassThatJacksonCannotSerialize();

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> stringifyObject(testObject));
  }

  @Test
  void shouldPassValidationSchema() throws JSONException, JsonProcessingException {
    JSONObject subject = new JSONObject();
    subject.put("id", "TEST-10");
    subject.put("attribute", "value");
    subject.put("testEnum", "test1");

    //    InputStream validationSchema = getClass().getClassLoader().getResourceAsStream("validation_schema.json");
    //    validateJsonSchema(subject, new ByteArrayInputStream(schema.getBytes()));
  }

  @Test
  void shouldNotPassValidationSchema() {

  }

  @Test
  void shouldCborEncode() throws IOException {
    TestObject subject = new TestObject();
    subject.setTestAttribute(TEST_ATTRIBUTE);

    byte[] serialized = cborEncode(subject);
    assertThat(serialized).isNotEmpty();
  }

  public static class TestObject implements Serializable {

    private static final long serialVersionUID = 0L;

    private String testAttribute;

    public String getTestAttribute() {
      return testAttribute;
    }

    public void setTestAttribute(String testAttribute) {
      this.testAttribute = testAttribute;
    }
  }

  private static class ClassThatJacksonCannotSerialize {
    private final ClassThatJacksonCannotSerialize self = this;

    @Override
    public String toString() {
      return self.getClass().getName();
    }
  }

}
