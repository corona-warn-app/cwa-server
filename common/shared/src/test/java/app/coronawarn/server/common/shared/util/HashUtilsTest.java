package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.HashUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.shared.util.HashUtils.MessageDigestAlgorithms;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import java.security.NoSuchAlgorithmException;

class HashUtilsTest {

  public static final String TEST_STRING = "string";

  @Test
  void testMd5DigestAsHex() {
    String hash = md5DigestAsHex(TEST_STRING);
    assertThat(hash).hasSize(32);
  }

  @Test
  void testRandomByteArrayData() {
    byte[] hash = generateSecureRandomByteArrayData(16);
    assertThat(hash).hasSize(16);
  }

  @Test
  void testByteStringDigest() {
    byte[] hash = byteStringDigest(ByteString.copyFromUtf8(TEST_STRING), MessageDigestAlgorithms.SHA_256);
    assertThat(hash).hasSize(32);
  }

  @Test
  void testDigestAsHex() throws NoSuchAlgorithmException {
    String hash = digestAsHex(TEST_STRING, MessageDigestAlgorithms.SHA_256);
    assertThat(hash).hasSize(64);
  }
}
