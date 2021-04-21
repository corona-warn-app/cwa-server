package app.coronawarn.server.common.persistence.utils.hash;

import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.springframework.util.DigestUtils;

public class HashUtils {

  public enum MessageDigestAlgorithms {
    SHA_256("SHA-256");

    private String name;

    MessageDigestAlgorithms(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private HashUtils() {
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

  /**
   * Generates a random byte array data for a specific size.
   *
   * @param size - byte array size
   * @return - random key
   */
  public static byte[] generateRandomKeyData(int size) {
    byte[] randomKeyData = new byte[size];
    new SecureRandom().nextBytes(randomKeyData);
    return randomKeyData;
  }

  /**
   * Returns a digested ByteString by a message digest object
   * that implements the chosen {@link MessageDigestAlgorithms}.
   *
   * @param locationId - ByteString to be diggested
   * @return - digested ByteString
   */
  public static byte[] hashLocationId(ByteString locationId, MessageDigestAlgorithms algorithm) {
    try {
      return MessageDigest.getInstance(algorithm.getName()).digest(locationId.toByteArray());
    } catch (NoSuchAlgorithmException e) {
      // DO NOTHING
    }
    return new byte[0];
  }

}
