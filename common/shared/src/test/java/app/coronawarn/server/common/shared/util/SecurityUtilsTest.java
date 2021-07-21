package app.coronawarn.server.common.shared.util;

import static app.coronawarn.server.common.shared.util.SecurityUtils.getEcdsaEncodeFromSignature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.binary.Hex;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import org.junit.Test;

public class SecurityUtilsTest {

  @Test
  public void testGetPublicKeyFromString() throws NoSuchAlgorithmException, InvalidKeySpecException {
    PublicKey key = SecurityUtils
        .getPublicKeyFromString("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEIxHvrv8jQx9OEzTZbsx1prQVQn"
            + "/3ex0gMYf6GyaNBW0QKLMjrSDeN6HwSPM0QzhvhmyQUixl6l88A7Zpu5OWSw==");
    assertEquals("EC", key.getAlgorithm());
    assertEquals("X.509", key.getFormat());
    assertThat(key.getEncoded()).isNotEmpty();
  }

  @Test
  public void testGetPrivateKeyFromString() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
    String signature = "+AE7sEXzNjgvxiDXrKdXQqL/XiOPIB/1r579jyIPWtQp7/a6K4m2vBsnjZSWvsZ+wT+WHkF8F64eCktNamZGhw==";
    byte[] base64DecodedSignature = Base64.getDecoder().decode(signature);

    assertThat(Hex.encodeHexString(getEcdsaEncodeFromSignature(base64DecodedSignature)))
        .isEqualTo("3045022100f8013bb045f336382fc620d7aca75742a2ff5e238f201ff5af9efd8f220f5ad4022029eff6ba2b89b6bc"
            + "1b278d9496bec67ec13f961e417c17ae1e0a4b4d6a664687");
  }
}
