package app.coronawarn.server.services.distribution.dgc.dsc;

import static app.coronawarn.server.common.shared.util.SecurityUtils.base64decode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class StringToByteForDscValuesConversionTest {

  @Test
  public void testForKidValuesDscDecodingAlgorithm() {
    String specExampleString = "3IsdmTYkAAM=";

    byte[] array = base64decode(specExampleString);
    String expectedHexString = "dc8b1d9936240003";
    String actualHexString = Hex.encodeHexString(array);
    assertThat(actualHexString).isEqualTo(expectedHexString);
  }
}
