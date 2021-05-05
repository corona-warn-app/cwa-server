package app.coronawarn.server.common.persistence.utils.hash;

import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(HashUtils.class);

  private HashUtils() {
  }

  /**
   * Generates a random byte array data for a specific size.
   *
   * @param size - byte array size
   * @return - random key
   */
  public static byte[] generateSecureRandomByteArrayData(int size) {
    byte[] randomKeyData = new byte[size];
    new SecureRandom().nextBytes(randomKeyData);
    return randomKeyData;
  }

  /**
   * Returns a digested ByteString by a Message Digest object
   * that implements the chosen {@link MessageDigestAlgorithms}.
   *
   * @param data - ByteString to be diggested
   * @return - digested ByteString
   */
  public static byte[] byteStringDigest(ByteString data, MessageDigestAlgorithms algorithm) {
    try {
      return MessageDigest.getInstance(algorithm.getName()).digest(data.toByteArray());
    } catch (NoSuchAlgorithmException e) {
      logger.error(e.getMessage(), e);
    }
    return new byte[0];
  }

  /**
   * Hash of the provided string with MD5 algorithm and return it as HEX.
   *
   * @param subject the string to compute the hash
   * @return the hash of the string.
   */
  public static String md5DigestAsHex(final String subject) {
    return DigestUtils.md5DigestAsHex(subject.getBytes(StandardCharsets.UTF_8));
  }

}
