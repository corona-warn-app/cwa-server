package app.coronawarn.server.services.distribution.statistics.file;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;

class JsonFileTest {
  @Test
  void testSetter() {
    final JsonFile fixture = new JsonFile(null, null);
    fixture.setETag("foo");
    final ByteArrayInputStream in = new ByteArrayInputStream("bar".getBytes());
    fixture.setContent(in);
    assertEquals(in, fixture.getContent());
    assertEquals("foo", fixture.getETag());
  }
}
