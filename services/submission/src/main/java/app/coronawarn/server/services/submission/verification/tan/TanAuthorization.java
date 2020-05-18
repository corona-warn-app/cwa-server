package app.coronawarn.server.services.submission.verification.tan;

import app.coronawarn.server.services.submission.verification.AuthorizationType;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This TAN Authorization holds all information needed in order to perform a verification on the Verification Server. It
 * supports the {@link AuthorizationType} and the actual TAN key.
 */
public class TanAuthorization {

  /**
   * the rough syntax of how an auth header value should look like. Note: Not yet final.
   */
  private static final Pattern SYNTAX = Pattern.compile("^(TAN|TELETAN) ([ a-zA-Z0-9]{6,30})$");

  private final AuthorizationType authType;

  private final String key;

  protected TanAuthorization(AuthorizationType authType, String key) {
    this.authType = authType;
    this.key = key;
  }

  public AuthorizationType getAuthType() {
    return authType;
  }

  public String getKey() {
    return key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TanAuthorization that = (TanAuthorization) o;
    return getAuthType() == that.getAuthType()
        && Objects.equals(getKey(), that.getKey());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAuthType(), getKey());
  }

  /**
   * Creates a new TanAuthorization object, based on the given authorization string value, which should be supplied as
   * the CWA-Authorization header upon uploading diagnosis keys.
   *
   * @param authValue The string auth value, e.g. TAN X32ZIWLL
   * @return the TAN Authorization instance, holding the auth type & auth key.
   * @throws TanAuthorizationException in case the validation on the given auth value string failed
   */
  public static TanAuthorization of(String authValue) throws TanAuthorizationException {
    if (authValue == null) {
      throw new TanAuthorizationException("No authorization value supplied.");
    }

    Matcher matcher = SYNTAX.matcher(authValue.trim());

    if (!matcher.matches()) {
      throw new IllegalTanAuthorizationFormatException(authValue);
    }

    AuthorizationType authType = AuthorizationType.from(matcher.group(1));
    String authKey = matcher.group(2);

    if (!authType.isValidSyntax(authKey)) {
      throw new IllegalTanAuthorizationKeyFormatException(authValue);
    }

    return new TanAuthorization(authType, authKey);
  }
}
