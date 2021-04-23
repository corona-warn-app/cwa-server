package app.coronawarn.server.common.shared.util;

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
  }

  /**
   * Generates a random byte array data for a specific size.
   *
   * @param size - byte array size
   * @return - random key
   */
  public static byte[] generateRandomByteArrayData(int size) {
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
      // DO NOTHING
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

  /**
   * Hash of the provided string with wanted algorithm and return it as HEX.
   * @param data - string to be hash
   * @param algorithm - algorithm to be used
   * @return - HEX string
   * @throws NoSuchAlgorithmException - thrown when the algorithm does not exist.
   */
  public static String digestAsHex(String data, MessageDigestAlgorithms algorithm) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(algorithm.getName());
    md.update(data.getBytes());
    return bytesToHex(md.digest());
  }

  /**
   * Transform bytes array into HEX.
   * @param bytes - array to be transformed
   * @return - Hex value
   */
  public static String bytesToHex(byte[] bytes) {
    StringBuffer result = new StringBuffer();
    for (byte byt : bytes) {
      result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
    }
    return result.toString();
  }
}
