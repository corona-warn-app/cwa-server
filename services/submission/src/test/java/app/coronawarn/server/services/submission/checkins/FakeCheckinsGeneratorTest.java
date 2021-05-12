package app.coronawarn.server.services.submission.checkins;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.shared.util.HashUtils;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

public class FakeCheckinsGeneratorTest {

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 15})
  public void should_generate_correct_number_of_checkins(Integer numberOfFakesToCreate) {
    FakeCheckinsGenerator underTest = new FakeCheckinsGenerator();

    Random random = new Random();
    int originalCheckinsListSize = random.nextInt(500);

    List<CheckIn> originalData = Stream.generate(this::randomCheckin)
        .limit(originalCheckinsListSize).collect(Collectors.toList());
    List<CheckIn> fakeCheckins =
        underTest.generateFakeCheckins(originalData, numberOfFakesToCreate, HashUtils.generateSecureRandomByteArrayData(16));

    assertThat(fakeCheckins).hasSize(originalCheckinsListSize * numberOfFakesToCreate);
  }

  @Test
  public void should_generate_fake_checkin_with_content_derived_from_original() {
    FakeCheckinsGenerator underTest = new FakeCheckinsGenerator();
    List<CheckIn> originalList = List.of(randomCheckin());
    byte[] pepper = HashUtils.generateSecureRandomByteArrayData(16);
    List<CheckIn> fakes = underTest.generateFakeCheckins(originalList, 1, pepper);

    assertThat(fakes).hasSize(1);
    assertContentDerivedCorrectly(originalList.iterator().next(), fakes.iterator().next(), pepper);
  }

  private void assertContentDerivedCorrectly(CheckIn original, CheckIn fake, byte[] pepper) {
    assertThat(fake.getLocationId()).isNotEmpty();
    assertThat(fake.getLocationId()).isNotEqualTo(original.getLocationId());
    assertThat(fake.getLocationId()
           .equals(original.getLocationId().concat(ByteString.copyFrom(pepper).concat(ByteString.copyFromUtf8("1"))))
    );
    assertThat(fake.getTransmissionRiskLevel()).isEqualTo(original.getTransmissionRiskLevel());
  }

  private CheckIn randomCheckin() {
    Random random = new Random();
    int boundedInt = random.nextInt(1000000);
    RandomStringUtils.randomAlphanumeric(10);

    return CheckIn.newBuilder().setStartIntervalNumber(boundedInt)
        .setEndIntervalNumber(boundedInt + 1000)
        .setTransmissionRiskLevel(random.nextInt(8))
        .setLocationId(ByteString.copyFromUtf8(RandomStringUtils.randomAlphanumeric(10)))
        .build();
  }
}
