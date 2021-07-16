package app.coronawarn.server.services.distribution.dgc.dsc;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StringToByteForDscValuesConversionTest {


  @Test
  public void testForKidValuesDscDecodingAlgorithm() {
    String specExampleString = "3IsdmTYkAAM=";

    byte[]  array = Base64.getDecoder().decode(specExampleString);
    String expectedHexString = "dc8b1d9936240003";
    String actualHexString = Hex.encodeHexString(array);
    assertThat(actualHexString).isEqualTo(expectedHexString);
  }

}
