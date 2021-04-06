package app.coronawarn.server.common.persistence.service;

import app.coronawarn.server.common.persistence.domain.TraceTimeIntervalWarning;
import app.coronawarn.server.common.persistence.repository.TraceTimeIntervalWarningRepository;
import app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification;
import app.coronawarn.server.common.persistence.service.utils.checkins.FakeCheckinsGenerator;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import com.google.protobuf.ByteString;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
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

  /**
   * Marker constant for the scenario where checkin location id hashing can not be computed.
   */
  private static final byte[] NO_HASH = new byte[0];

  private final TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo;
  private final FakeCheckinsGenerator fakeCheckinsGenerator;

  public TraceTimeIntervalWarningService(
      TraceTimeIntervalWarningRepository traceTimeIntervalWarningRepo,
      FakeCheckinsGenerator fakeCheckinsGenerator) {
    this.traceTimeIntervalWarningRepo = traceTimeIntervalWarningRepo;
    this.fakeCheckinsGenerator = fakeCheckinsGenerator;
  }

  /**
   * Store the given checkin data as {@link TraceTimeIntervalWarning} entities. Returns the number
   * of inserted entities, which is useful for the case where there might be conflicts with the
   * table constraints during the db save operations.
   */
  @Transactional
  public int saveCheckins(List<CheckIn> checkins) {
    return saveCheckins(checkins, this::hashLocationId);
  }

  private int saveCheckins(List<CheckIn> checkins, Function<ByteString, byte[]> idHashGenerator) {
    int numberOfInsertedTraceWarnings = 0;

    for (CheckIn checkin : checkins) {
      byte[] hashId = idHashGenerator.apply(checkin.getLocationId());
      if (hashId != NO_HASH) {
        boolean traceWarningInsertedSuccessfully = traceTimeIntervalWarningRepo
            .saveDoNothingOnConflict(hashId,
                checkin.getStartIntervalNumber(),
                checkin.getEndIntervalNumber() - checkin.getStartIntervalNumber(),
                checkin.getTransmissionRiskLevel(),
                CheckinsDateSpecification.HOUR_SINCE_EPOCH_DERIVATION
                    .apply(Instant.now().getEpochSecond()));

        if (traceWarningInsertedSuccessfully) {
          numberOfInsertedTraceWarnings++;
        }
      }
    }

    int conflictingTraceWarnings = checkins.size() - numberOfInsertedTraceWarnings;
    if (conflictingTraceWarnings > 0) {
      logger.warn(
          "{} out of {} TraceTimeIntervalWarnings conflicted with existing "
          + "database entries or had errors while storing "
              + "and were ignored.",
          conflictingTraceWarnings, checkins.size());
    }

    return numberOfInsertedTraceWarnings;
  }

  /**
   * For each checkin in the given list, generate other fake checkin data based on the passed in
   * number and store everything as {@link TraceTimeIntervalWarning} entities. Returns the number of
   * inserted entities which is useful for the case where there might be conflicts with the table
   * constraints during the db save operations.
   */
  @Transactional
  public int saveCheckinsWithFakeData(List<CheckIn> originalCheckins, int numberOfFakesToCreate) {
    List<CheckIn> allCheckins = new ArrayList<>(originalCheckins);
    allCheckins.addAll(fakeCheckinsGenerator.generateFakeCheckins(originalCheckins,
        numberOfFakesToCreate, randomHashPepper()));
    return saveCheckins(allCheckins, this::hashLocationId);
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
    try {
      return MessageDigest.getInstance("SHA-256").digest(locationId.toByteArray());
    } catch (NoSuchAlgorithmException e) {
      logger.warn("Could not apply SHA-256 hash on " + locationId
          + " while storing TraceTimeIntervalWarnings");
    }
    return NO_HASH;
  }

  private byte[] randomHashPepper() {
    byte[] pepper = new byte[16];
    new SecureRandom().nextBytes(pepper);
    return pepper;
  }
}