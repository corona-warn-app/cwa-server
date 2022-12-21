package app.coronawarn.server.services.submission.verification;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OtpState {
  EXPIRED("expired"),
  REDEEMED("redeemed"),
  VALID("valid");

  private final String state;

  OtpState(final String state) {
    this.state = state;
  }

  @JsonValue
  final String state() {
    return state;
  }

  @Override
  public String toString() {
    return state;
  }
}
