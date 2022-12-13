package app.coronawarn.server.services.submission.verification;

public class SrsOtpRedemptionResponse {

  private String otp;

  private OtpState state;

  private boolean strongClientIntegrityCheck;

  /**
   * Constructor.
   *
   * @param otp                        The SRS one time password .
   * @param state                      The SRS OTP state.
   * @param strongClientIntegrityCheck The strongClientIntegrityCheck.
   */
  public SrsOtpRedemptionResponse(final String otp, final OtpState state, final boolean strongClientIntegrityCheck) {
    this.otp = otp;
    this.state = state;
    this.strongClientIntegrityCheck = strongClientIntegrityCheck;
  }

  public String getOtp() {
    return otp;
  }

  public OtpState getState() {
    return state;
  }

  public boolean isStrongClientIntegrityCheck() {
    return strongClientIntegrityCheck;
  }

  public void setOtp(final String otp) {
    this.otp = otp;
  }

  public void setState(final OtpState state) {
    this.state = state;
  }

  public void setStrongClientIntegrityCheck(final boolean strongClientIntegrityCheck) {
    this.strongClientIntegrityCheck = strongClientIntegrityCheck;
  }

  @Override
  public String toString() {
    return "{\"otp\":\"" + otp + "\",\"state\":\"" + state + "\",\"strongClientIntegrityCheck\":"
        + strongClientIntegrityCheck + "}";
  }
}
