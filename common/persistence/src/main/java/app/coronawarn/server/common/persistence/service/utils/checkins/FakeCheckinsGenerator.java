package app.coronawarn.server.common.persistence.service.utils.checkins;

import static app.coronawarn.server.common.persistence.service.utils.checkins.FakeCheckinIntervalSpecification.*;

import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import com.google.protobuf.ByteString;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class FakeCheckinsGenerator {

  private final Random random = new SecureRandom();

  /**
   * For each checkin in the original list, create X amount of fake checkins and collect to one
   * single list which is returned. X is equal to the method's second parameter.
   *
   * @param pepper Optional element used to compute the hashes of the fake checkins.
   */
  public List<CheckIn> generateFakeCheckins(List<CheckIn> checkins, int numberOfFakesToCreate,
      final byte[] pepper) {
    return checkins.stream()
        .flatMap(original -> IntStream.range(0, numberOfFakesToCreate)
            .mapToObj(counter -> buildFakeCheckin(original, String.valueOf(counter),
                (pepper == null || pepper.length == 0) ? randomHashPepper() : pepper)))
        .collect(Collectors.toList());
  }

  private byte[] randomHashPepper() {
    byte[] pepper = new byte[16];
    random.nextBytes(pepper);
    return pepper;
  }

  private CheckIn buildFakeCheckin(CheckIn original, String counter, byte[] pepper) {
    return CheckIn.newBuilder()
        .setLocationId(original.getLocationId()
            .concat(ByteString.copyFrom(pepper).concat(ByteString.copyFromUtf8(counter))))
        .setTransmissionRiskLevel(original.getTransmissionRiskLevel())
        .setStartIntervalNumber(START_INTERVAL_GENERATION.apply(original))
        .setEndIntervalNumber(END_INTERVAL_GENERATION.apply(original))
        .build();
  }

}