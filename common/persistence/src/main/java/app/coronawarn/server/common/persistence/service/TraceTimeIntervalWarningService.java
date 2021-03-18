package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.eventregistration.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.protocols.internal.evreg.CheckIn;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TraceTimeIntervalWarningService {

  private static final Logger logger =
      LoggerFactory.getLogger(TraceTimeIntervalWarningService.class);

  private final TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo;

  public TraceTimeIntervalWarningService(
      TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo) {
    this.traceTimeIntervalWarningRepo = traceTimeIntervalWarningRepo;
  }

  @Transactional
  public int saveCheckinData(List<CheckIn> checkins) {
    int numberOfInsertedCheckins = 0;

    for (CheckIn checkin : checkins) {
      boolean keyInsertedSuccessfully = true;
//      boolean keyInsertedSuccessfully = traceTimeIntervalWarningRepo.saveDoNothingOnConflict(
//          );

      if (keyInsertedSuccessfully) {
        numberOfInsertedCheckins++;
      }
    }

    int conflictingKeys = checkins.size() - numberOfInsertedCheckins;
    if (conflictingKeys > 0) {
      logger.warn(
          "{} out of {} TraceTimeIntervalWarnings conflicted with existing database entries and were ignored.",
          conflictingKeys, checkins.size());
    }

    return numberOfInsertedCheckins;
  }
}
