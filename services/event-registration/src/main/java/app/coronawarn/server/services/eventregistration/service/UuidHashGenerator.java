package app.coronawarn.server.services.eventregistration.service;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;


public class UuidHashGenerator {

  public static String buildUuidHash() throws NoSuchAlgorithmException {
    return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
        .digest(UUID
            .randomUUID()
            .toString()
            .getBytes(
                Charset.defaultCharset())));
  }
}
