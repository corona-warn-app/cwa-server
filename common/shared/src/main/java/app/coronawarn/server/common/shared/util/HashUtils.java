package app.coronawarn.server.common.shared.util;

import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

public class HashUtils {

  private static final Logger logger = LoggerFactory.getLogger(HashUtils.class);

  public enum Algorithms {
    SHA_256("SHA-256"),
    EC("EC"),
    SHA_ECDSA("SHA256withECDSA");

    private String name;

    Algorithms(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

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
   * Returns a digested String by a Message Digest object
   * that implements the chosen {@link Algorithms}.
   *
   * @param data - String to be diggested
   * @return - digested ByteString
   */
  public static byte[] byteStringDigest(String data, Algorithms algorithm) {
    try {
      return MessageDigest.getInstance(algorithm.getName()).digest(data.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      logger.error("Digest algorithm does not exist", e);
    }
    return new byte[0];
  }

  /**
   * Returns a digested ByteString by a Message Digest object
   * that implements the chosen {@link Algorithms}.
   *
   * @param data - ByteString to be diggested
   * @return - digested ByteString
   */
  public static byte[] byteStringDigest(ByteString data, Algorithms algorithm) {
    try {
      return MessageDigest.getInstance(algorithm.getName()).digest(data.toByteArray());
    } catch (NoSuchAlgorithmException e) {
      logger.error("Digest algorithm does not exist", e);
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
