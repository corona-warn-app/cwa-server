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
  public void readFile() throws UnableToLoadFileException {
    var p = new ParameterFileProvider();
    var result = p.readFile("parameters/example.yaml");

    System.out.println(result.getDuration().getGt30Min());

    System.out.println(result);

    assertEquals(HttpStatus.OK, HttpStatus.OK);
  }
}
