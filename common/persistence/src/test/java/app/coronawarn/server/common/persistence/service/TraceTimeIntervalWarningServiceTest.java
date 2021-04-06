package app.coronawarn.server.common.persistence.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import com.google.protobuf.ByteString;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

@DataJdbcTest
class TraceTimeIntervalWarningServiceTest {

  @Autowired
  TraceTimeIntervalWarningService traceWarningsService;

  @Autowired
  TraceTimeIntervalWarningRepository traceWarningsRepository;

  @BeforeEach
  public void setup() {
    traceWarningsRepository.deleteAll();
  }

  @Test
  void testStorage() {
    List<CheckIn> checkins = getRandomTestData();
    traceWarningsService.saveCheckins(checkins);

    List<TraceTimeIntervalWarning> actualTraceWarningsStored =
        StreamSupport.stream(traceWarningsRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());

    assertCheckinsAndWarningsAreEqual(new ArrayList<>(checkins), actualTraceWarningsStored);
  }

  @Test
  void testStorageWithRandomPadding() {
    List<CheckIn> checkins = getRandomTestData();
    traceWarningsService.saveCheckinsWithFakeData(checkins, 2);

    List<TraceTimeIntervalWarning> actualTraceWarningsStored =
        StreamSupport.stream(traceWarningsRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    assertTrue(actualTraceWarningsStored.size() == checkins.size() + checkins.size() * 2);
  }

  private List<CheckIn> getRandomTestData() {
    List<CheckIn> checkins = List.of(
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
    return checkins;
  }

  private void assertCheckinsAndWarningsAreEqual(List<CheckIn> checkins,
      List<TraceTimeIntervalWarning> actualTraceWarningsStored) {

    assertEquals(checkins.size(), actualTraceWarningsStored.size());

    Collections.sort(checkins, Comparator.comparing(CheckIn::getTransmissionRiskLevel));
    Collections.sort(actualTraceWarningsStored,
        Comparator.comparing(TraceTimeIntervalWarning::getTransmissionRiskLevel));

    for (int i = 0; i < checkins.size(); i++) {
      CheckIn checkin = checkins.get(i);
      TraceTimeIntervalWarning warning = actualTraceWarningsStored.get(i);
      assertEquals(checkin.getTransmissionRiskLevel(),
          warning.getTransmissionRiskLevel().intValue());
      assertEquals(checkin.getStartIntervalNumber(), warning.getStartIntervalNumber().intValue());
      assertEquals(checkin.getEndIntervalNumber() - checkin.getStartIntervalNumber(), warning.getPeriod().intValue());
      assertArrayEquals(hashLocationId(checkin.getLocationId()),
          warning.getTraceLocationId());
    }
  }

  @Test
  void testSortedRetrievalResult() {
      traceWarningsRepository
          .saveDoNothingOnConflict(hashLocationId(ByteString.copyFromUtf8("sorted-uuid2")),
              56,
              10,
              3,
              CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
                  .apply(Instant.now().getEpochSecond()));
    traceWarningsRepository
        .saveDoNothingOnConflict(hashLocationId(ByteString.copyFromUtf8("sorted-uuid1")),
            456,
            20,
            2,
            CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
                .apply(Instant.now().getEpochSecond()) - 10);

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

    List<TraceTimeIntervalWarning> checkinsFromDB = new ArrayList<>(traceWarningsService.getTraceTimeIntervalWarnings());

    assertCheckinsAndWarningsAreEqual(checkins, checkinsFromDB);
  }


  private byte[] hashLocationId(ByteString locationId) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(locationId.toByteArray());
    } catch (NoSuchAlgorithmException e) {
    }
    return new byte[0];
  }
}
