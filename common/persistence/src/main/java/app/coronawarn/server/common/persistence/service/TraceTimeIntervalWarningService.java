package app.coronawarn.server.common.persistence.service;

import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.shared.util.HashUtils.MessageDigestAlgorithms;
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

  private final TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo;
  private final MessageDigest hashAlgorithm;

  /**
   * Constructs the service instance.
   *
   * @param traceTimeIntervalWarningRepo Repository for {@link TraceTimeIntervalWarning} entities.
   * @throws NoSuchAlgorithmException In case the MessageDigest used in hashing can not be instantiated.
   */
  public TraceTimeIntervalWarningService(
      TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo) throws NoSuchAlgorithmException {
    this.traceTimeIntervalWarningRepo = traceTimeIntervalWarningRepo;
    this.hashAlgorithm = MessageDigest.getInstance(MessageDigestAlgorithms.SHA_256.getName());
  }

  /**
   * Store the given checkin data as {@link TraceTimeIntervalWarning} entities. Returns the number of inserted entities,
   * which is useful for the case where there might be conflicts with the table constraints during the db save
   * operations.
   */
  @Transactional
  public int saveCheckins(List<CheckIn> checkins, int submissionTimestamp, SubmissionType submissionType) {
    return saveCheckins(checkins, this::hashLocationId, submissionTimestamp, submissionType);
  }

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
   * Returns all available {@link TraceTimeIntervalWarning}s sorted by their submissionTimestamp.
   */
  public Collection<TraceTimeIntervalWarning> getTraceTimeIntervalWarnings() {
    return StreamUtils
        .createStreamFromIterator(traceTimeIntervalWarningRepo
            .findAll(Sort.by(Direction.ASC, "submissionTimestamp")).iterator())
        .collect(Collectors.toList());
  }

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
  }
}
