package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
      boolean traceWarningInsertedSuccessfully = traceTimeIntervalWarningRepo.saveDoNothingOnConflict(
          checkin.getSignedLocation().getLocation().toByteArray(),
          checkin.getStartIntervalNumber(), checkin.getEndIntervalNumber(),
          checkin.getTransmissionRiskLevel());

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
}