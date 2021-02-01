package app.coronawarn.server.services.callback;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.util.encoders.Hex;

public class CallbackUtils {

  /**
   * Computes the SHA-256 hash of the provided string.
   *
   * @param subject the string to compute the hash
   * @return the SHA-256 hash of the string.
   */
  public static String computeSha256Hash(String subject) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Hash algorithm not found.");
    }
    byte[] hash = digest.digest(
        subject.getBytes(StandardCharsets.UTF_8));
    return new String(Hex.encode(hash));
  }

}
