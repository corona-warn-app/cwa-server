package app.coronawarn.server.services.submission.verification.tan;

import app.coronawarn.server.services.submission.verification.AuthorizationType;

public class TanTestCase {

  private String authValue;

  private TanAuthorization expected;

  private TanTestCase(String authValue) {
    this.authValue = authValue;
  }

  public static TanTestCase with(String authValue) {
    return new TanTestCase(authValue);
  }

  public TanTestCase expect(AuthorizationType type, String key) {
    this.expected = new TanAuthorization(type, key);
    return this;
  }

  public TanAuthorization getExpected() {
    return expected;
  }

  public String getAuthValue() {
    return authValue;
  }
}
