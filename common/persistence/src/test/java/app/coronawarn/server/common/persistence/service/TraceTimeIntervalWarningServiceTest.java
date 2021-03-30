package app.coronawarn.server.common.persistence.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import com.google.protobuf.ByteString;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;

@DataJdbcTest
class TraceTimeIntervalWarningServiceTest {

  @Autowired
  TraceTimeIntervalWarningService traceWarningsService;

  @Autowired
  TraceTimeIntervalWarningRepository traceWarningsRepository;

  @Test
  void testStorage() {
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

    traceWarningsService.saveCheckinData(checkins);

    List<TraceTimeIntervalWarning> actualTraceWarningsStored =
        StreamSupport.stream(traceWarningsRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());

    assertCheckinsAndWarningsAreEqual(new ArrayList<>(checkins), actualTraceWarningsStored);
  }

  private void assertCheckinsAndWarningsAreEqual(List<CheckIn> checkins,
      List<TraceTimeIntervalWarning> actualTraceWarningsStored) {

    assertEquals(checkins.size(), actualTraceWarningsStored.size());

    Collections.sort(checkins, (c1, c2) -> Integer.valueOf(c1.getTransmissionRiskLevel())
        .compareTo(Integer.valueOf(c2.getTransmissionRiskLevel())));
    Collections.sort(actualTraceWarningsStored,
        (c1, c2) -> Integer.valueOf(c1.getTransmissionRiskLevel())
            .compareTo(Integer.valueOf(c2.getTransmissionRiskLevel())));

    for (int i = 0; i < checkins.size(); i++) {
      CheckIn checkin = checkins.get(i);
      TraceTimeIntervalWarning warning = actualTraceWarningsStored.get(i);
      assertEquals(checkin.getTransmissionRiskLevel(),
          warning.getTransmissionRiskLevel().intValue());
      assertEquals(checkin.getStartIntervalNumber(), warning.getStartIntervalNumber().intValue());
      assertEquals(checkin.getEndIntervalNumber(), warning.getEndIntervalNumber().intValue());
      assertArrayEquals(checkin.getLocationId().toByteArray(),
          warning.getTraceLocationId());
    }
  }
}