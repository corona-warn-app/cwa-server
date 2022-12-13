package app.coronawarn.server.services.submission.verification;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class SrsOtpRedemptionResponse {

  private String otp;

  private OtpState state;

  private boolean strongClientIntegrityCheck;

  /**
   * Required for automatic JSON deserialization.
   */
  public SrsOtpRedemptionResponse() {
  }

  /**
   * Required for automatic JSON deserialization.
   *
   * @param json - {@link String} representation of this class. See {@link #toString()}.
   * @throws IOException when there is an issue with {@link ObjectMapper} or {@link JsonParser#readValueAs(Class)}
   */
  public SrsOtpRedemptionResponse(final String json) throws IOException {
    final SrsOtpRedemptionResponse me = new ObjectMapper().createParser(json)
        .readValueAs(SrsOtpRedemptionResponse.class);
    otp = me.otp;
    state = me.state;
    strongClientIntegrityCheck = me.strongClientIntegrityCheck;
  }

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

  public void setState(final String state) {
    this.state = state == null ? null : OtpState.valueOf(state.toUpperCase());
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
