package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.HashUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import app.coronawarn.server.common.shared.util.HashUtils.MessageDigestAlgorithms;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

class HashUtilsTest {

  public static final String TEST_STRING = "string";
  public static final String HEX_DIGESTED_ORIGIN = "test string";
  public static final String HEX_DIGESTED = "6f8db599de986fab7a21625b7916589c";

  @Test
  void testMd5DigestAsHex() {
    String hash = md5DigestAsHex(HEX_DIGESTED_ORIGIN);
    assertThat(hash).hasSize(32);
    assertEquals(hash, HEX_DIGESTED);
  }

  @Test
  void testRandomByteArrayData() {
    byte[] hash = generateSecureRandomByteArrayData(16);
    byte[] hash2 = generateSecureRandomByteArrayData(16);

    assertThat(hash).hasSize(16);
    assertThat(hash2).hasSize(16);
    assertThat(hash).isNotEqualTo(hash2);
  }

  @Test
  void testByteStringDigest() {
    byte[] hash = byteStringDigest(ByteString.copyFromUtf8(TEST_STRING), MessageDigestAlgorithms.SHA_256);
    assertThat(hash).hasSize(32);
  }
}
