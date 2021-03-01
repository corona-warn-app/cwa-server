package app.coronawarn.server.services.callback;

import java.nio.charset.StandardCharsets;
import org.springframework.util.DigestUtils;

public class HashingUtils {

  private HashingUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Computes the hash of the provided string.
   *
   * @param subject the string to compute the hash
   * @return the hash of the string.
   */
  public static String computeHash(final String subject) {
    return DigestUtils.md5DigestAsHex(subject.getBytes(StandardCharsets.UTF_8));
  }
}
