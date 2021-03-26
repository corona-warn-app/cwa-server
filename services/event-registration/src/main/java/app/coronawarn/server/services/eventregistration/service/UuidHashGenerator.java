package app.coronawarn.server.services.eventregistration.service;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;


public class UuidHashGenerator {

  /**
   * Generates a valid UUID and hashes it with SHA-256.
   *
   * @return a valid hashed UUID.
   * @throws NoSuchAlgorithmException thrown if algorithm does not exist.
   */
  public static String buildUuidHash() throws NoSuchAlgorithmException {
    return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
        .digest(UUID
            .randomUUID()
            .toString()
            .getBytes(
                Charset.defaultCharset())));
  }
}
