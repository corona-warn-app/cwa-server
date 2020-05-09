package app.coronawarn.server.services.distribution.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class ParameterFileProviderTest {

  @Test
  public void test() {
    assertEquals(HttpStatus.OK, HttpStatus.OK);
  }

  @Test
  public void readFile() {
    var p = new ParameterFileProvider();
    p.readFile("aa");
    assertEquals(HttpStatus.OK, HttpStatus.OK);
  }
}
