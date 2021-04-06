package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.utils.CheckinsDateSpecification;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TraceTimeIntervalWarningService {

  private static final Logger logger =
      LoggerFactory.getLogger(TraceTimeIntervalWarningService.class);

  private final TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo;

  public TraceTimeIntervalWarningService(
      TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo) {
    this.traceTimeIntervalWarningRepo = traceTimeIntervalWarningRepo;
  }

  /**
   * Store the given checkin data as {@link TraceTimeIntervalWarning} entities. Returns the number
   * of inserted entities useful for the case where there might be conflicts with the table
   * constraints during the db save operations.
   */
  @Transactional
  public int saveCheckinData(List<CheckIn> checkins) {
    int numberOfInsertedTraceWarnings = 0;

    for (CheckIn checkin : checkins) {
      boolean traceWarningInsertedSuccessfully = traceTimeIntervalWarningRepo
          .saveDoNothingOnConflict(checkin.getLocationId().toByteArray(),
              checkin.getStartIntervalNumber(),
              checkin.getEndIntervalNumber() - checkin.getStartIntervalNumber(),
              checkin.getTransmissionRiskLevel(),
              CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
                  .apply(Instant.now().getEpochSecond()));

      if (traceWarningInsertedSuccessfully) {
        numberOfInsertedTraceWarnings++;
      }
    }

    int conflictingTraceWarnings = checkins.size() - numberOfInsertedTraceWarnings;
    if (conflictingTraceWarnings > 0) {
      logger.warn(
          "{} out of {} TraceTimeIntervalWarnings conflicted with existing database entries and were ignored.",
          conflictingTraceWarnings, checkins.size());
    }

    return numberOfInsertedTraceWarnings;
  }

  /**
   * Returns all available {@link TraceTimeIntervalWarning}s sorted by their submissionTimestamp.
   */
  public Collection<TraceTimeIntervalWarning> getTraceTimeIntervalWarnings() {
    return StreamUtils
        .createStreamFromIterator(traceTimeIntervalWarningRepo
            .findAll(Sort.by(Direction.ASC, "submissionTimestamp")).iterator())
        .collect(Collectors.toList());
  }
}
