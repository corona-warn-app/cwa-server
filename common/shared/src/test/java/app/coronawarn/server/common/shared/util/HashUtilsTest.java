package app.coronawarn.server.common.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.shared.util.HashUtils.MessageDigestAlgorithms;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

class HashUtilsTest {

  public static final String TEST_STRING = "string";

  @Test
  void testHashHasExpectedLength() {
    String hash = HashUtils.md5DigestAsHex(TEST_STRING);
    assertThat(hash).hasSize(32);
  }

  @Test
  void testRandomKeyDataSize() {
    byte[] hash = HashUtils.generateRandomByteArrayData(16);
    assertThat(hash).hasSize(16);
  }

  @Test
  void hashLocationIdSize() {
    byte[] hash = HashUtils.byteStringDigest(ByteString.copyFromUtf8(TEST_STRING), MessageDigestAlgorithms.SHA_256);
    assertThat(hash).hasSize(32);
  }

}
