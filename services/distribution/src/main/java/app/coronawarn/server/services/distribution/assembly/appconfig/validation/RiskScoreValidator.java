

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

/**
 * Validates a risk score value according to Exposure Notification API by Google/Apple.
 */
public class RiskScoreValidator {

  private RiskScoreValidator() {

  }

  /**
   * Validates the bounds of a risk score value.
   *
   * @param value the risk score value
   * @return true if value is within bounds, false otherwise
   */
  public static boolean isWithinBounds(int value) {
    return ParameterSpec.RISK_SCORE_MIN <= value && value <= ParameterSpec.RISK_SCORE_MAX;
  }
}
