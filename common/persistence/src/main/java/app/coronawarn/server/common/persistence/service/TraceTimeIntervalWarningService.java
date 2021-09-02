package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static app.coronawarn.server.common.shared.util.HashUtils.Algorithms.SHA_256;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.CheckInProtectedReports;
import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.CheckInProtectedReportsRepository;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import com.google.protobuf.ByteString;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
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

  @Deprecated(since = "2.8", forRemoval = true)
  private final TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo;
  private final CheckInProtectedReportsRepository checkInProtectedReportsRepository;
  @Deprecated(since = "2.8", forRemoval = true)
  private final MessageDigest hashAlgorithm;

  /**
   * Constructs the service instance.
   *
   * @param traceTimeIntervalWarningRepo Repository for {@link TraceTimeIntervalWarning} entities.
   * @throws NoSuchAlgorithmException In case the MessageDigest used in hashing can not be instantiated.
   */
  public TraceTimeIntervalWarningService(
      TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo,
      CheckInProtectedReportsRepository checkInProtectedReportsRepository) throws NoSuchAlgorithmException {
    this.traceTimeIntervalWarningRepo = traceTimeIntervalWarningRepo;
    this.hashAlgorithm = MessageDigest.getInstance(SHA_256.getName());
    this.checkInProtectedReportsRepository = checkInProtectedReportsRepository;
  }

  /**
   * Store the given checkin data as {@link TraceTimeIntervalWarning} entities. Returns the number of inserted entities,
   * which is useful for the case where there might be conflicts with the table constraints during the db save
   * operations.
   *
   * @deprecated in favor of encrypted checkins.
   */
  @Deprecated(since = "2.8", forRemoval = true)
  @Transactional
  public int saveCheckins(List<CheckIn> checkins, int submissionTimestamp, SubmissionType submissionType) {
    return saveCheckins(checkins, this::hashLocationId, submissionTimestamp, submissionType);
  }

  @Deprecated(since = "2.8", forRemoval = true)
  private int saveCheckins(List<CheckIn> checkins, Function<ByteString, byte[]> idHashGenerator,
      int submissionTimestamp, SubmissionType submissionType) {
    int numberOfInsertedTraceWarnings = 0;

    for (CheckIn checkin : checkins) {
      byte[] hashId = idHashGenerator.apply(checkin.getLocationId());
      boolean traceWarningInsertedSuccessfully = traceTimeIntervalWarningRepo
          .saveDoNothingOnConflict(hashId, checkin.getStartIntervalNumber(),
              checkin.getEndIntervalNumber() - checkin.getStartIntervalNumber(),
              checkin.getTransmissionRiskLevel(),
              submissionTimestamp,
              submissionType.name());

      if (traceWarningInsertedSuccessfully) {
        numberOfInsertedTraceWarnings++;
      }
    }

    int conflictingTraceWarnings = checkins.size() - numberOfInsertedTraceWarnings;
    if (conflictingTraceWarnings > 0) {
      logger.warn(
          "{} out of {} TraceTimeIntervalWarnings conflicted with existing database entries or had errors while "
              + "storing and were ignored.",
          conflictingTraceWarnings, checkins.size());
    }

    return numberOfInsertedTraceWarnings;
  }

  /**
   * Store the given checkin data as {@link TraceTimeIntervalWarning} entities for the Protected Reports. Returns the
   * number of inserted entities, which is useful for the case where there might be conflicts with the table constraints
   * during the db save operations.
   */
  @Transactional
  public int saveCheckInProtectedReports(List<CheckInProtectedReport> allCheckins, Integer submissionTimestamp) {
    int numberOfCheckInProtectedReports = 0;
    for (CheckInProtectedReport checkInProtectedReports : allCheckins) {
      boolean checkInProtectedInsertedSuccessfully = checkInProtectedReportsRepository
          .saveDoNothingOnConflict(checkInProtectedReports.getLocationIdHash().toByteArray(),
              checkInProtectedReports.getIv().toByteArray(),
              checkInProtectedReports.getEncryptedCheckInRecord().toByteArray(),
              checkInProtectedReports.getMac().toByteArray(),
              submissionTimestamp);

      if (checkInProtectedInsertedSuccessfully) {
        numberOfCheckInProtectedReports++;
      }
    }
    if (allCheckins.size() != numberOfCheckInProtectedReports && allCheckins.size() > 0) {
      logger.error("Couldn't save all ({}) received encrypted checkins. Stored only {}!", allCheckins.size(),
          numberOfCheckInProtectedReports);
    }
    return numberOfCheckInProtectedReports;
  }

  /**
   * Returns all available {@link TraceTimeIntervalWarning}s sorted by their submissionTimestamp.
   *
   * @deprecated because trace time warnings are not longer supported and replaced by encrypted checkins.
   */
  @Deprecated(since = "2.8", forRemoval = true)
  public Collection<TraceTimeIntervalWarning> getTraceTimeIntervalWarnings() {
    return StreamUtils
        .createStreamFromIterator(traceTimeIntervalWarningRepo
            .findAll(Sort.by(Direction.ASC, "submissionTimestamp")).iterator())
        .collect(Collectors.toList());
  }

  /**
   * Returns all available {@link CheckInProtectedReports}s sorted by their submissionTimestamp.
   */
  public Collection<CheckInProtectedReports> getCheckInProtectedReports() {
    return StreamUtils
        .createStreamFromIterator(checkInProtectedReportsRepository
            .findAll(Sort.by(Direction.ASC, "submissionTimestamp")).iterator())
        .collect(Collectors.toList());
  }

  @Deprecated(since = "2.8", forRemoval = true)
  private byte[] hashLocationId(ByteString locationId) {
    return hashAlgorithm.digest(locationId.toByteArray());
  }

  /**
   * Deletes all trace time warning entries which have a submission timestamp that is older than the specified number of
   * days.
   *
   * @param daysToRetain the number of days until which trace time warnings will be retained.
   * @throws IllegalArgumentException if {@code daysToRetain} is negative.
   */
  @Transactional
  public void applyRetentionPolicy(int daysToRetain) {
    if (daysToRetain < 0) {
      throw new IllegalArgumentException("Number of days to retain must be greater or equal to 0.");
    }

    long threshold = LocalDateTime
        .ofInstant(Instant.now(), UTC)
        .minusDays(daysToRetain)
        .toEpochSecond(UTC) / SECONDS_PER_HOUR;
    int numberOfDeletions = traceTimeIntervalWarningRepo.countOlderThan(threshold);
    logger.info("Deleting {} trace time warning(s) with a submission timestamp older than {} day(s) ago.",
        numberOfDeletions, daysToRetain);
    traceTimeIntervalWarningRepo.deleteOlderThan(threshold);

    numberOfDeletions = checkInProtectedReportsRepository.countOlderThan(threshold);
    logger.info("Deleting {} encrypted trace time warning(s) with a submission timestamp older than {} day(s) ago.",
        numberOfDeletions, daysToRetain);
    checkInProtectedReportsRepository.deleteOlderThan(threshold);
  }
}
