package app.coronawarn.server.services.submission.checkins;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.service.TraceTimeIntervalWarningService;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.CheckinsStorageResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventCheckinFacade {

  private static final Logger logger = LoggerFactory.getLogger(EventCheckinFacade.class);

  public static final Marker EVENT = MarkerFactory.getMarker("EVENT");

  private TraceTimeIntervalWarningService traceTimeIntervalWarningService;
  private FakeCheckinsGenerator fakeCheckinsGenerator;
  private final EventCheckinDataFilter checkinsDataFilter;
  private final SubmissionServiceConfig submissionServiceConfig;


  /**
   * Create a new instance.
   */
  public EventCheckinFacade(TraceTimeIntervalWarningService traceTimeIntervalWarningService,
      FakeCheckinsGenerator fakeCheckinsGenerator,
      EventCheckinDataFilter checkinsDataFilter,
      SubmissionServiceConfig submissionServiceConfig) {
    this.traceTimeIntervalWarningService = traceTimeIntervalWarningService;
    this.fakeCheckinsGenerator = fakeCheckinsGenerator;
    this.checkinsDataFilter = checkinsDataFilter;
    this.submissionServiceConfig = submissionServiceConfig;
  }

  /**
   * For each checkin in the given list, generate other fake checkin data based on the passed in number and store
   * everything as {@link TraceTimeIntervalWarning} entities. Returns the number of inserted entities which is useful
   * for the case where there might be conflicts with the table constraints during the db save operations.
   *
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  public int saveCheckinsWithFakeData(List<CheckIn> originalCheckins, int numberOfFakesToCreate,
      byte[] pepper, int submissionTimestamp, SubmissionType submissionType) {
    List<CheckIn> allCheckins = new ArrayList<>(originalCheckins);
    allCheckins.addAll(fakeCheckinsGenerator.generateFakeCheckins(originalCheckins,
        numberOfFakesToCreate, pepper));
    return traceTimeIntervalWarningService.saveCheckins(allCheckins, submissionTimestamp, submissionType);
  }

  /**
   * Extract check-ins from submission payload and store them.
   *
   * @param submissionPayload - submission payload
   * @return - storage result containing number of filtered and saved check-ins.
   * @deprecated because trace time warnings are being replaced by protected reports.
   */
  @Deprecated(since = "2.8")
  private CheckinsStorageResult extractAndStoreEventCheckins(SubmissionPayload submissionPayload) {
    // need a container object that reflects how many checkins were filtered even if storage fails
    AtomicInteger numberOfFilteredCheckins = new AtomicInteger(0);
    AtomicInteger numberOfSavedCheckins = new AtomicInteger(0);
    try {
      checkinsDataFilter.validateCheckInsByDate(submissionPayload.getCheckInsList());
      List<CheckIn> checkins = checkinsDataFilter.filter(submissionPayload.getCheckInsList());
      numberOfFilteredCheckins.set(submissionPayload.getCheckInsList().size() - checkins.size());
      numberOfSavedCheckins.set(saveCheckinsWithFakeData(checkins,
          submissionServiceConfig.getRandomCheckinsPaddingMultiplier(),
          submissionServiceConfig.getRandomCheckinsPaddingPepperAsByteArray(),
          CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION.apply(Instant.now().getEpochSecond()),
          submissionPayload.getSubmissionType()));
    } catch (final TooManyCheckInsAtSameDay e) {
      logger.error(EVENT, e.getMessage());
    } catch (final Exception e) {
      // Any check-in data processing related error must not interrupt the submission flow or interfere
      // with storing of the diagnosis keys
      logger.error(EVENT, "An error has occured while trying to store the event checkin data", e);
    }
    return new CheckinsStorageResult(numberOfFilteredCheckins.get(), numberOfSavedCheckins.get());
  }

  /**
   * Stores the encrypted checkins.
   *
   * @param checkInProtectedReports List of checkins
   * @return the number of saved checkins.
   */
  private CheckinsStorageResult saveCheckInProtectedReports(List<CheckInProtectedReport> checkInProtectedReports) {
    Integer submissionTimestamp = CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
        .apply(Instant.now().getEpochSecond());
    int numberOfSavedCheckins = traceTimeIntervalWarningService
        .saveCheckInProtectedReports(checkInProtectedReports, submissionTimestamp);
    logger.debug("Successfully saved {} protected reports", numberOfSavedCheckins);
    return new CheckinsStorageResult(0, numberOfSavedCheckins);
  }

  /**
   * Extract and store checkins. Used for unencrypted checkins and encrypted check ins and returns statistics about the
   * saving process.
   *
   * @param submissionPayload the payload where to extract the checkins from.
   * @return an instance of {@link CheckinsStorageResult} that represents how many check ins were saved and filtered.
   */
  public CheckinsStorageResult extractAndStoreCheckins(SubmissionPayload submissionPayload) {
    CheckinsStorageResult checkinsStorageResult = new CheckinsStorageResult(0, 0);

    if (submissionServiceConfig.isUnencryptedCheckinsEnabled()) {
      CheckinsStorageResult other = this.extractAndStoreEventCheckins(submissionPayload);
      checkinsStorageResult = checkinsStorageResult.update(other);
    }
    CheckinsStorageResult saved = this.saveCheckInProtectedReports(submissionPayload.getCheckInProtectedReportsList());
    checkinsStorageResult = checkinsStorageResult.update(saved);
    return checkinsStorageResult;
  }
}
