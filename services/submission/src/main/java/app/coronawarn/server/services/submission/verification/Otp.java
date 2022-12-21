package app.coronawarn.server.services.submission.verification;

import java.util.Objects;
import java.util.UUID;

/**
 * A representation of a One-Time-Passcode (OTP).
 */
public class Otp {
  /**
   * Creates a new {@link #Otp} instance for the given tan string.
   *
   * @param tan A valid UUID string representation.
   * @return The Otp instance
   * @throws IllegalArgumentException when the given tan string is not a valid UUID.
   */
  public static Otp of(final Tan tan) {
    return new Otp(tan.getTan());
  }

  private final UUID otp;

  private Otp(final UUID otp) {
    this.otp = otp;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Otp other = (Otp) o;
    return otp.equals(other.otp);
  }

  /**
   * Returns the OTP entity as UUID.
   *
   * @return the {@link #otp}.
   */
  public UUID getOtp() {
    return otp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(otp);
  }

  /**
   * Returns the OTP in it's string representation.
   *
   * @return the tan UUID as a string.
   */
  @Override
  public String toString() {
    return otp.toString();
  }
}
