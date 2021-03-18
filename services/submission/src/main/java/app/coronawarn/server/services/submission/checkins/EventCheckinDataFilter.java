package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.services.submission.checkins.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.evreg.CheckIn;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EventCheckinDataFilter {

  private final SubmissionServiceConfig submissionServiceConfig;
  private final TraceLocationSignatureVerifier traceLocationSignatureVerifier;

  public EventCheckinDataFilter(SubmissionServiceConfig submissionServiceConfig,
      TraceLocationSignatureVerifier traceLocationSignatureVerifier) {
    this.submissionServiceConfig = submissionServiceConfig;
    this.traceLocationSignatureVerifier = traceLocationSignatureVerifier;
  }

  /**
   * Given the payload extract the checkin data and return a filtered list based on the
   * following criteria:
   * <li>Filter out checkins with TRL values that are mapped to 0 ( as per Risk calculation paramters app config) </li>
   * <li>Filter out checkins which have checkout time in the past further than 15 days (app config)</li>
   * <li>Filter out checkins which are in the future</li>
   * <li>Filter out checkins which have trace location signatures that can not be verified</li>.
   */
  public List<CheckIn> extractAndFilter(SubmissionPayload submissionPayload) {
    List<CheckIn> checkins = submissionPayload.getCheckInsList();
    return checkins.stream()
        .filter(this::filterByValidSignature)
        .filter(this::filterByTransmissionRiskLevel)
        .filter(this::filterOutOldCheckins)
        .filter(this::filterOutFutureCheckins).collect(Collectors.toList());
  }


  private boolean filterByTransmissionRiskLevel(CheckIn checkin) {
    // TODO: This requires a refactoring work to extract Risk calculation parameters in a another yaml that is
    // shareable across distribution and submission services
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

  private boolean filterByValidSignature(CheckIn checkin) {
    return traceLocationSignatureVerifier.verify(checkin.getSignedEvent());
  }
}
