package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.SerializationUtils.deserializeJson;
import static app.coronawarn.server.common.shared.util.SerializationUtils.stringifyObject;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;


class SerializationUtilsTest {

  public static final String TEST_ATTRIBUTE = "testAttribute";
  public static final String TEST_ATTRIBUTE_VALUE = "test-value";
  public static final String TEST_OBJECT_SERIALIZED = "{\"testAttribute\":\"test-value\"}";
  public static final String TEST_OBJECT_SERIALIZED_WRONG_FORMAT = "{\"testAttribute\"\"test-value\"}";

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

  public static class TestObject implements Serializable {

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
