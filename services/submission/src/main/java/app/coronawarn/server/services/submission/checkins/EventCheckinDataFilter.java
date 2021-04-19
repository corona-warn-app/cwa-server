package app.coronawarn.server.services.submission.checkins;

import static app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static app.coronawarn.server.services.submission.controller.SubmissionController.EVENT;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.config.PreDistributionTrlValueMappingProvider;
import app.coronawarn.server.common.persistence.domain.config.TransmissionRiskValueMapping;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventCheckinDataFilter {

  private static final Logger logger = LoggerFactory.getLogger(EventCheckinDataFilter.class);

  private final SubmissionServiceConfig submissionServiceConfig;
  private final PreDistributionTrlValueMappingProvider trlValueMappingProvider;

  /**
   * Creates am instance.
   */
  public EventCheckinDataFilter(SubmissionServiceConfig submissionServiceConfig,
      PreDistributionTrlValueMappingProvider trlValueMappingProvider) {
    this.submissionServiceConfig = submissionServiceConfig;
    this.trlValueMappingProvider = trlValueMappingProvider;
  }

  /**
   * Return a filtered list of checkin data based on the following criteria:
   * <li>Filter out checkins with TRL values that are mapped to 0 ( as per Risk calculation paramters app config)</li>
   * <li>Filter out checkins which have checkout time in the past further than 15 days (app config)</li>
   * <li>Filter out checkins which are in the future</li>
   * <li>Filter out checkins which have trace location signatures that can not be verified</li>.
   */
  public List<CheckIn> filter(List<CheckIn> checkins) {
    AtomicInteger checkinsPickedAfterTrlFiltering = new AtomicInteger();
    AtomicInteger checkinsPickedAfterOldFiltering = new AtomicInteger();
    AtomicInteger checkinsPickedAfterFutureFiltering = new AtomicInteger();
    final var filtered = checkins.stream()
        .filter(this::filterOutZeroTransmissionRiskLevel)
        .peek(k -> checkinsPickedAfterTrlFiltering.incrementAndGet())
        .filter(this::filterOutOldCheckins)
        .peek(k -> checkinsPickedAfterOldFiltering.incrementAndGet())
        .filter(this::filterOutFutureCheckins)
        .peek(k -> checkinsPickedAfterFutureFiltering.incrementAndGet())
        .collect(Collectors.toList());

    logger.info(EVENT, "Filtering of {} checkins started", checkins.size());
    logger.info(EVENT, "{} checkins remaining after filtering out zero TRLs", checkinsPickedAfterOldFiltering.get());
    logger.info(EVENT, "{} checkins remaining after filtering out old checkins", checkinsPickedAfterFutureFiltering.get());
    logger.info(EVENT, "{} checkins remaining after filtering out future checkins", checkinsPickedAfterFutureFiltering.get());

    return filtered;
  }

  /**
   * If there are more than N check-ins for any date, the submission is considered fraudulent and processing of
   * check-ins is aborted.
   *
   * @param checkins list of check-ins to be validated.
   * @throws TooManyCheckInsAtSameDay if more than N check-ins for any date exist.
   */
  public void validateCheckInsByDate(final List<CheckIn> checkins) throws TooManyCheckInsAtSameDay {
    final Map<Integer, Integer> counterPerDays = new HashMap<>();
    for (CheckIn checkIn : checkins) {
      final int day = checkIn.getStartIntervalNumber() / 144;
      int count = counterPerDays.getOrDefault(day, 0);
      if (++count > submissionServiceConfig.getMaxAllowedCheckinsPerDay()) {
        throw new TooManyCheckInsAtSameDay(day);
      }
      counterPerDays.put(day, count);
    }
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

  private boolean mapsTo(Integer valueToCheck, Double target) {
    Optional<TransmissionRiskValueMapping> foundTarget =
        trlValueMappingProvider.getTransmissionRiskValueMapping().stream()
            .filter(m -> m.getTransmissionRiskLevel().equals(valueToCheck)).findAny();
    return foundTarget.isPresent() && foundTarget.get().getTransmissionRiskValue().equals(target);
  }
}
