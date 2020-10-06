

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

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

  /**
   * The allowed minimum value for risk score.
   */
  public static final int RISK_SCORE_MIN = 0;

  /**
   * The allowed maximum value for a risk score.
   */
  public static final int RISK_SCORE_MAX = 9999;

  /**
   * The allowed minimum value for an attenuation threshold.
   */
  public static final int ATTENUATION_DURATION_THRESHOLD_MIN = 0;

  /**
   * The allowed maximum value for an attenuation threshold.
   */
  public static final int ATTENUATION_DURATION_THRESHOLD_MAX = 100;

  /**
   * The allowed minimum value for an attenuation weight.
   */
  public static final double ATTENUATION_DURATION_WEIGHT_MIN = .0;

  /**
   * The allowed maximum value for an attenuation weight.
   */
  public static final double ATTENUATION_DURATION_WEIGHT_MAX = 1.;

  /**
   * Maximum number of allowed decimals for an attenuation weight.
   */
  public static final int ATTENUATION_DURATION_WEIGHT_MAX_DECIMALS = 3;


  /**
   * The allowed minimum value for a default bucket offset.
   */
  public static final int DEFAULT_BUCKET_OFFSET_MIN = 0;

  /**
   * The allowed maximum value for a default bucket offset.
   */
  public static final int DEFAULT_BUCKET_OFFSET_MAX = 1;

  /**
   * The allowed minimum value for a risk score normalization divisor.
   */
  public static final int RISK_SCORE_NORMALIZATION_DIVISOR_MIN = 0;

  /**
   * The allowed maximum value for a risk score normalization divisor.
   */
  public static final int RISK_SCORE_NORMALIZATION_DIVISOR_MAX = 1000;
}
