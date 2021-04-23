package app.coronawarn.server.common.persistence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.common.shared.util.HashUtils.MessageDigestAlgorithms;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.testcontainers.shaded.org.bouncycastle.util.encoders.Hex;

@DataJdbcTest
class TraceTimeIntervalWarningServiceTest {

  @Autowired
  TraceTimeIntervalWarningService traceWarningsService;

  @Autowired
  TraceTimeIntervalWarningRepository traceWarningsRepository;

  private int currentTimestamp;

  @BeforeEach
  public void setup() {
    traceWarningsRepository.deleteAll();
    currentTimestamp = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(Instant.now().getEpochSecond());
  }

  @Test
  void testStorage() {
    List<CheckIn> checkins = getRandomTestData();
    traceWarningsService.saveCheckins(checkins, currentTimestamp,
        SubmissionType.SUBMISSION_TYPE_PCR_TEST);

    List<TraceTimeIntervalWarning> actualTraceWarningsStored =
        StreamSupport.stream(traceWarningsRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());

    assertCheckinsAndWarningsAreEqual(checkins, actualTraceWarningsStored);
  }

  @Test
  void testSortedRetrievalResult() {
    traceWarningsRepository
        .saveDoNothingOnConflict(
            HashUtils.byteStringDigest(ByteString.copyFromUtf8("sorted-uuid2"), MessageDigestAlgorithms.SHA_256),
            56, 10, 3,
            CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(Instant.now().getEpochSecond()),
            SubmissionType.SUBMISSION_TYPE_PCR_TEST.name());
    traceWarningsRepository
        .saveDoNothingOnConflict(
            HashUtils.byteStringDigest(ByteString.copyFromUtf8("sorted-uuid1"), MessageDigestAlgorithms.SHA_256),
            456, 20, 2,
            CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(Instant.now().getEpochSecond()) - 10,
            SubmissionType.SUBMISSION_TYPE_PCR_TEST.name());

    List<CheckIn> checkins = new ArrayList<>(List.of(
        CheckIn.newBuilder().setStartIntervalNumber(56).setEndIntervalNumber(66)
            .setTransmissionRiskLevel(3)
            .setLocationId(ByteString.copyFromUtf8("sorted-uuid2"))
            .build(),
        CheckIn.newBuilder().setStartIntervalNumber(456).setEndIntervalNumber(476)
            .setTransmissionRiskLevel(2)
            .setLocationId(ByteString.copyFromUtf8("sorted-uuid1"))
            .build()));

    // Reverse as we tempered with submission timestamp
    Collections.reverse(checkins);

    var checkinsFromDB = traceWarningsService.getTraceTimeIntervalWarnings();

    assertCheckinsAndWarningsAreEqual(checkins, checkinsFromDB);
  }

  /**
   * Contract for hashing between client and server.
   */
  @Test
  public void testHashingOfTraceLocationId() {
    String locationId = "afa27b44d43b02a9fea41d13cedc2e4016cfcf87c5dbf990e593669aa8ce286d";
    byte[] locationIdByte = Hex.decode(locationId);
    byte[] hashedLocationId = HashUtils.byteStringDigest(ByteString.copyFrom(locationIdByte), MessageDigestAlgorithms.SHA_256);

    final byte[] encode = Hex.encode(hashedLocationId);
    String s = new String(encode);

    assertEquals("0f37dac11d1b8118ea0b44303400faa5e3b876da9d758058b5ff7dc2e5da8230", s);
  }

  @DisplayName("Assert a positive retention period is accepted.")
  @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
  @ParameterizedTest
  void testApplyRetentionPolicyForValidNumberOfDays(int daysToRetain) {
    assertThatCode(() -> traceWarningsService.applyRetentionPolicy(daysToRetain))
        .doesNotThrowAnyException();
  }

  @DisplayName("Assert a negative retention period is rejected.")
  @ValueSource(ints = {Integer.MIN_VALUE, -1})
  @ParameterizedTest
  void testApplyRetentionPolicyForNegativeNumberOfDays(int daysToRetain) {
    assertThat(catchThrowable(() -> traceWarningsService.applyRetentionPolicy(daysToRetain)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testApplyRetentionPolicyForEmptyDb() {
    traceWarningsService.applyRetentionPolicy(1);
    var actKeys = traceWarningsService.getTraceTimeIntervalWarnings();
    assertThat(actKeys).isEmpty();
  }

  @Test
  void testApplyRetentionPolicyForNotApplicableEntries() {
    var expKeys = getRandomTestData();

    traceWarningsService.saveCheckins(expKeys, currentTimestamp, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    traceWarningsService.applyRetentionPolicy(1);
    var actKeys = traceWarningsService.getTraceTimeIntervalWarnings();

    assertCheckinsAndWarningsAreEqual(expKeys, actKeys);
  }

  @Test
  void testApplyRetentionPolicyForOneApplicableEntry() {
    var keys = getRandomTestData();

    traceWarningsService.saveCheckins(keys, currentTimestamp - (int) TimeUnit.DAYS.toHours(1) - 1,
        SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    traceWarningsService.applyRetentionPolicy(1);
    var actKeys = traceWarningsService.getTraceTimeIntervalWarnings();

    assertThat(actKeys).isEmpty();
  }

  private List<CheckIn> getRandomTestData() {
    return List.of(
        CheckIn.newBuilder().setStartIntervalNumber(0).setEndIntervalNumber(1)
            .setTransmissionRiskLevel(1)
            .setLocationId(ByteString.copyFromUtf8("uuid1"))
            .build(),
        CheckIn.newBuilder().setStartIntervalNumber(23).setEndIntervalNumber(30)
            .setTransmissionRiskLevel(2)
            .setLocationId(ByteString.copyFromUtf8("uuid1"))
            .build(),
        CheckIn.newBuilder().setStartIntervalNumber(40).setEndIntervalNumber(50)
            .setTransmissionRiskLevel(3)
            .setLocationId(ByteString.copyFromUtf8("uuid1"))
            .build());
  }

  private void assertCheckinsAndWarningsAreEqual(Collection<CheckIn> checkins,
      Collection<TraceTimeIntervalWarning> actualTraceWarningsStored) {

    assertEquals(checkins.size(), actualTraceWarningsStored.size());

    var sortedCheckins = checkins.stream()
        .sorted(Comparator.comparing(CheckIn::getTransmissionRiskLevel))
        .collect(Collectors.toList());
    var sortedTraceTimeWarnings = actualTraceWarningsStored.stream()
        .sorted(Comparator.comparing(TraceTimeIntervalWarning::getTransmissionRiskLevel))
        .collect(Collectors.toList());

    for (int i = 0; i < checkins.size(); i++) {
      CheckIn checkin = sortedCheckins.get(i);
      TraceTimeIntervalWarning warning = sortedTraceTimeWarnings.get(i);
      assertEquals(checkin.getTransmissionRiskLevel(),
          warning.getTransmissionRiskLevel().intValue());
      assertEquals(checkin.getStartIntervalNumber(), warning.getStartIntervalNumber().intValue());
      assertEquals(checkin.getEndIntervalNumber() - checkin.getStartIntervalNumber(), warning.getPeriod().intValue());
      assertArrayEquals(HashUtils.byteStringDigest(checkin.getLocationId(), MessageDigestAlgorithms.SHA_256),
          warning.getTraceLocationId());
    }
  }

}
