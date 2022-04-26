package app.coronawarn.server.common.persistence.domain;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class RevocationEtagTest {

  @Test
  void testConstructorWithParams() {
    RevocationEtag fixture = new RevocationEtag("", "");
    assertEquals("", fixture.getPath());
    assertEquals("", fixture.getEtag());
    RevocationEtag fixture2 = new RevocationEtag("foo", "bar");
    assertEquals("foo", fixture2.getPath());
    assertEquals("bar", fixture2.getEtag());
  }
}
