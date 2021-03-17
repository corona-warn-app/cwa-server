package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.services.submission.checkins.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.evreg.CheckIn;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;

@Component
public class EventCheckinDataFilter {

  private final SubmissionServiceConfig submissionServiceConfig;
  private final TraceLocationSignatureVerifier traceLocationSignatureVerifier;

  public EventCheckinDataFilter(SubmissionServiceConfig submissionServiceConfig,
      TraceLocationSignatureVerifier traceLocationSignatureVerifier) {
    this.submissionServiceConfig = submissionServiceConfig;
    this.traceLocationSignatureVerifier = traceLocationSignatureVerifier;
  }

  public List<CheckIn> extractAndFilter(SubmissionPayload submissionPayload) {
    List<CheckIn> checkins = submissionPayload.getCheckInsList();
    return checkins.stream()
        .filter(this::filterByValidSignature)
        .filter(this::filterByTransmissionRiskLevel)
        .filter(this::filterOutOldCheckins)
        .filter(this::filterOutFutureCheckins).collect(Collectors.toList());
  }


  private boolean filterByTransmissionRiskLevel(CheckIn checkin) {
    // TODO: Clarify with Max
    return true;
  }

  private boolean filterOutOldCheckins(CheckIn checkin) {
    Integer acceptableTimeframeInDays = submissionServiceConfig.getAcceptedEventDateThresholdDays();
    int threshold = TEN_MINUTE_INTERVAL_DERIVATION.apply(LocalDateTime.ofInstant(Instant.now(), UTC)
        .minusDays(acceptableTimeframeInDays).toEpochSecond(UTC));
    return threshold < checkin.getCheckoutTime();
  }

  private boolean filterOutFutureCheckins(CheckIn checkin) {
    int threshold = TEN_MINUTE_INTERVAL_DERIVATION
        .apply(LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC));
    return threshold > checkin.getCheckinTime();
  }

  /**
   * Pick the given checkin if the signature of the trace location is valid.
   */
  private boolean filterByValidSignature(CheckIn checkin) {
    return traceLocationSignatureVerifier.verify(checkin.getSignedEvent());
  }
}
