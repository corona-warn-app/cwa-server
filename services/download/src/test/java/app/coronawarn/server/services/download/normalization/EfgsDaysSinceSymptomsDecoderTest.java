package app.coronawarn.server.services.download.normalization;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class EfgsDaysSinceSymptomsDecoderTest {

  @ParameterizedTest
  @MethodSource("originalAndExpectedDSOS")
  void dsosShouldBeDecodedCorrecly(Pair<Integer, Integer> originalAndExpected) {
    EfgsDaysSinceSymptomsDecoder decoder = new EfgsDaysSinceSymptomsDecoder();
    int decodedDsos = decoder.decode(originalAndExpected.getLeft());
    assertEquals(originalAndExpected.getRight(), decodedDsos);
  }

  @ParameterizedTest
  @ValueSource(ints = {22, 50, 77, 133, 140, 240, 340, 440, 540, 640, 750, 850, 925, 1170})
  void errorShouldBeThrownWhenDecodedValueIsNonCompliantExposureNotifications(Integer value) {
    assertThrows(DecodedDsosNotInExposureNotificationFrameworkRange.class, () -> {
      EfgsDaysSinceSymptomsDecoder decoder = new EfgsDaysSinceSymptomsDecoder();
      decoder.decode(value);
    });
  }

  @ParameterizedTest
  @ValueSource(ints = {-15, 2175, 3175, 4001})
  void errorShouldBeThrownWhenDsosIsNotPartOfRangeSpecifications(Integer value) {
    assertThrows(IllegalArgumentException.class, () -> {
      EfgsDaysSinceSymptomsDecoder decoder = new EfgsDaysSinceSymptomsDecoder();
      decoder.decode(value);
    });
  }

  private static Stream<Arguments> originalAndExpectedDSOS() {
    return Stream.of(
     Pair.of(10,10),
     Pair.of(96,-4),
     Pair.of(201,1),
     Pair.of(195,-5),
     Pair.of(296,-4),
     Pair.of(314,14),
     Pair.of(496,-4),
     Pair.of(596,-4),
     Pair.of(696,-4),
     Pair.of(796,-4),
     Pair.of(896,-4),
     Pair.of(996,-4),
     Pair.of(1186,-14),
     Pair.of(1200,0),
     Pair.of(1997,-3),
     Pair.of(2998,-2),
     Pair.of(3993,-7),
     Pair.of(4000,0)
    ).map(Arguments::of);
  }
}
