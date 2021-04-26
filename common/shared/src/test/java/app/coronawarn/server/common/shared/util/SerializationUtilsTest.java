package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.SerializationUtils.deserializeJson;
import static app.coronawarn.server.common.shared.util.SerializationUtils.stringifyObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import org.junit.jupiter.api.Test;


class SerializationUtilsTest {

  public static final String TEST_ATTRIBUTE = "testAttribute";
  public static final String TEST_ATTRIBUTE_VALUE = "test-value";
  public static final String TEST_OBJECT_SERIALIZED = "{\"testAttribute\":\"test-value\"}";

  @Test
  void testDeserializeJson() {
    TestObject testObject = deserializeJson(TEST_OBJECT_SERIALIZED,
        typeFactory -> typeFactory.constructType(TestObject.class));

    assertEquals(TEST_ATTRIBUTE_VALUE, testObject.getTestAttribute());
  }

  @Test
  void testStringifyObject() {
    TestObject testObject = new TestObject();
    testObject.setTestAttribute(TEST_ATTRIBUTE_VALUE);
    String stringify = stringifyObject(testObject);

    assertTrue(stringify.contains(TEST_ATTRIBUTE));
    assertTrue(stringify.contains(TEST_ATTRIBUTE_VALUE));
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
}
