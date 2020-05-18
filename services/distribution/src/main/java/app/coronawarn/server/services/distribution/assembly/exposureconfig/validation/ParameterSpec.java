package app.coronawarn.server.services.distribution.assembly.exposureconfig.validation;

/**
 * Definition of the spec according to Apple/Google:
 * https://developer.apple.com/documentation/exposurenotification/enexposureconfiguration
 */
public class ParameterSpec {

  private ParameterSpec() {
  }

  /**
   * The minimum weight value for mobile API.
   */
  public static final double WEIGHT_MIN = 0.001;

  /**
   * The maximum weight value for mobile API.
   */
  public static final int WEIGHT_MAX = 100;

  /**
   * Maximum number of allowed decimals.
   */
  public static final int WEIGHT_MAX_DECIMALS = 3;

}
