package app.coronawarn.server.services.submission.checkins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.shared.util.HashUtils;
import app.coronawarn.server.services.submission.controller.CheckinsStorageResult;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@DirtiesContext
@ActiveProfiles("integration-test")
class EventCheckinsFacadeIT {

  @Autowired
  private EventCheckinFacade eventCheckinFacade;

  @Autowired
  TraceTimeIntervalWarningRepository traceWarningsRepository;

  @MockBean
  private TestRestTemplate testRestTemplate;

  private int currentTimestamp;

  @BeforeEach
  public void setup() {
    traceWarningsRepository.deleteAll();
    currentTimestamp = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(Instant.now().getEpochSecond());
  }

  @Test
  void testExtractAndStoreEventCheckins() {
    final SubmissionPayload newPayload = SubmissionPayload.newBuilder()
        .addAllCheckIns(getRandomTestData())
        .build();

    CheckinsStorageResult result = eventCheckinFacade.extractAndStoreEventCheckins(newPayload);
    assertEquals(result.getNumberOfFilteredCheckins(), 2);
    assertEquals(result.getNumberOfSavedCheckins(), 2);
  }

  @Test
  void testStorageWithRandomPadding() {
    List<CheckIn> checkins = getRandomTestData();
    eventCheckinFacade.saveCheckinsWithFakeData(checkins, 2, HashUtils.generateSecureRandomByteArrayData(16),
        currentTimestamp, SubmissionType.SUBMISSION_TYPE_PCR_TEST);

    List<TraceTimeIntervalWarning> actualTraceWarningsStored =
        StreamSupport.stream(traceWarningsRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    Assert.assertEquals(actualTraceWarningsStored.size(), checkins.size() + checkins.size() * 2);
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
        CheckIn.newBuilder().setStartIntervalNumber(40).setEndIntervalNumber(3006923)
            .setTransmissionRiskLevel(3)
            .setLocationId(ByteString.copyFromUtf8("uuid1"))
            .build());
    return checkins;
  }

}
