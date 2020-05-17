package app.coronawarn.server.services.submission.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


public class AuthorizationTypeTest {

  @Test
  public void checkTanSyntax() {
    assertEquals(true, AuthorizationType.TAN.isValidSyntax("ANY SYNTAX"));
  }

  @Test
  public void checkTeleTanSyntax() {
    assertEquals(true, AuthorizationType.TELETAN.isValidSyntax("ANY SYNTAX"));
  }
}
