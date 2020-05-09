package app.coronawarn.server.services.distribution.parameters.validation;

/**
 * Definition of the spec according to Apple/Google:
 * https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration
 */
public class ParameterSpec {

  public static final int WEIGHT_MIN = 0;

  public static final int WEIGHT_MAX = 100;

  public static final int WEIGHT_MAX_DECIMALS = 3;

}
