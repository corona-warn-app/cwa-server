package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.common.persistence.utils.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.config.PreDistributionTrlValueMappingProvider;
import app.coronawarn.server.common.persistence.domain.config.TransmissionRiskValueMapping;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EventCheckinDataFilter {

  private final SubmissionServiceConfig submissionServiceConfig;
  private final TraceLocationSignatureVerifier traceLocationSignatureVerifier;
  private final PreDistributionTrlValueMappingProvider trlValueMappingProvider;

  /**
   * Creates am instance.
   */
  public EventCheckinDataFilter(SubmissionServiceConfig submissionServiceConfig,
      TraceLocationSignatureVerifier traceLocationSignatureVerifier,
      PreDistributionTrlValueMappingProvider trlValueMappingProvider) {
    this.submissionServiceConfig = submissionServiceConfig;
    this.traceLocationSignatureVerifier = traceLocationSignatureVerifier;
    this.trlValueMappingProvider = trlValueMappingProvider;
  }

  /**
   * Return a filtered list of checkin data based on the following criteria:
   * <li>Filter out checkins with TRL values that are mapped to 0 ( as per Risk calculation paramters app config) </li>
   * <li>Filter out checkins which have checkout time in the past further than 15 days (app config)</li>
   * <li>Filter out checkins which are in the future</li>
   * <li>Filter out checkins which have trace location signatures that can not be verified</li>.
   */
  public List<CheckIn> filter(List<CheckIn> checkins) {
    return checkins.stream()
        .filter(this::filterByValidSignature)
        .filter(this::filterOutZeroTransmissionRiskLevel)
        .filter(this::filterOutOldCheckins)
        .filter(this::filterOutFutureCheckins).collect(Collectors.toList());
  }


  boolean filterOutZeroTransmissionRiskLevel(CheckIn checkin) {
    return !mapsTo(checkin.getTransmissionRiskLevel(), 0.0d);
  }

  boolean filterOutOldCheckins(CheckIn checkin) {
    Integer acceptableTimeframeInDays = submissionServiceConfig.getAcceptedEventDateThresholdDays();
    int threshold = TEN_MINUTE_INTERVAL_DERIVATION.apply(LocalDateTime.ofInstant(Instant.now(), UTC)
        .minusDays(acceptableTimeframeInDays).toEpochSecond(UTC));
    return threshold < checkin.getEndIntervalNumber();
  }

  boolean filterOutFutureCheckins(CheckIn checkin) {
    int threshold = TEN_MINUTE_INTERVAL_DERIVATION
        .apply(LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC));
    return threshold > checkin.getStartIntervalNumber();
  }

  boolean filterByValidSignature(CheckIn checkin) {
    return traceLocationSignatureVerifier.verify(checkin.getSignedLocation());
  }

  private boolean mapsTo(Integer valueToCheck, Double target) {
    Optional<TransmissionRiskValueMapping> foundTarget =
        trlValueMappingProvider.getTransmissionRiskValueMapping().stream()
            .filter(m -> m.getTransmissionRiskLevel().equals(valueToCheck)).findAny();
    return foundTarget.isPresent() && foundTarget.get().getTransmissionRiskValue().equals(target);
  }
}