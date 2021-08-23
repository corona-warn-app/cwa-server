package app.coronawarn.server.services.submission.integration;

import static app.coronawarn.server.common.persistence.service.utils.checkins.CheckinsDateSpecification.TEN_MINUTE_INTERVAL_DERIVATION;
import static app.coronawarn.server.common.shared.util.HashUtils.generateSecureRandomByteArrayData;
import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildTemporaryExposureKeyWithoutMultiplying;
import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildTemporaryExposureKeys;
import static java.time.ZoneOffset.UTC;

import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.Builder;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.checkins.EventCheckinDataValidatorTest;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class DataHelpers {

  public static SubmissionPayload buildSubmissionPayload(List<String> visitedCountries, String originCountry,
      Boolean consentToFederation, List<TemporaryExposureKey> temporaryExposureKeys,
      SubmissionType submissionType) {

    Builder submissionPayloadBuilder = SubmissionPayload
        .newBuilder()
        .addAllKeys(temporaryExposureKeys);

    if (visitedCountries != null) {
      submissionPayloadWithVisitedCountries(submissionPayloadBuilder, visitedCountries);
    }
    if (originCountry != null) {
      submissionPayloadWithOriginCountry(submissionPayloadBuilder, originCountry);
    }
    if (consentToFederation != null) {
      submissionPayloadWithConsentToFederation(submissionPayloadBuilder, consentToFederation);
    }
    if (submissionType != null) {
      submissionPayloadWithSubmissionType(submissionPayloadBuilder, submissionType);
    }

    SubmissionPayload submissionPayload = submissionPayloadBuilder.build();
    return submissionPayload;
  }

  public static CheckInProtectedReport buildEncryptedCheckIn(ByteString checkInRecord, ByteString iv,
      ByteString locationIdHash, ByteString mac) {
    return CheckInProtectedReport.newBuilder()
        .setEncryptedCheckInRecord(checkInRecord)
        .setIv(iv)
        .setLocationIdHash(locationIdHash)
        .setMac(mac)
        .build();
  }

  public static CheckInProtectedReport buildDefaultEncryptedCheckIn() {
    return buildEncryptedCheckIn(ByteString.copyFrom(generateSecureRandomByteArrayData(16)),
        ByteString.copyFrom(generateSecureRandomByteArrayData(16)),
        ByteString.copyFrom(generateSecureRandomByteArrayData(32)),
        ByteString.copyFrom(generateSecureRandomByteArrayData(32))
    );
  }

  public static CheckInProtectedReport buildDefaultEncryptedCheckIn(byte[] locationIdHash) {
    return buildEncryptedCheckIn(ByteString.copyFrom(generateSecureRandomByteArrayData(16)),
        ByteString.copyFrom(generateSecureRandomByteArrayData(16)),
        ByteString.copyFrom(locationIdHash),
        ByteString.copyFrom(generateSecureRandomByteArrayData(32)));
  }

  public static CheckIn buildCheckIn(int startInterval, int endInterval, int transmissionRiskLevel,
      ByteString locationIdHash) {
    return CheckIn.newBuilder().setStartIntervalNumber(startInterval)
        .setEndIntervalNumber(endInterval)
        .setTransmissionRiskLevel(transmissionRiskLevel)
        .setLocationId(locationIdHash)
        .build();
  }

  public static CheckIn buildDefaultCheckIn() {
    return buildCheckIn(TEN_MINUTE_INTERVAL_DERIVATION
            .apply(LocalDateTime.ofInstant(Instant.now(), UTC).minusDays(1).toEpochSecond(UTC)),
        TEN_MINUTE_INTERVAL_DERIVATION
            .apply(LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC)),
        3,
        EventCheckinDataValidatorTest.CORRECT_LOCATION_ID);
  }

  public static CheckIn buildDefaultCheckIn(byte[] locationId) {
    return buildCheckIn(TEN_MINUTE_INTERVAL_DERIVATION
            .apply(LocalDateTime.ofInstant(Instant.now(), UTC).minusDays(1).toEpochSecond(UTC)),
        TEN_MINUTE_INTERVAL_DERIVATION
            .apply(LocalDateTime.ofInstant(Instant.now(), UTC).toEpochSecond(UTC)),
        3,
        ByteString.copyFrom(locationId));
  }


  public static SubmissionPayload buildSubmissionPayloadWithCheckins(List<String> visitedCountries,
      String originCountry,
      Boolean consentToFederation, List<TemporaryExposureKey> temporaryExposureKeys,
      SubmissionType submissionType, List<CheckInProtectedReport> protectedReports, List<CheckIn> checkins) {

    Builder submissionPayloadBuilder = SubmissionPayload
        .newBuilder()
        .addAllKeys(temporaryExposureKeys)
        .addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkins);

    if (visitedCountries != null) {
      submissionPayloadWithVisitedCountries(submissionPayloadBuilder, visitedCountries);
    }
    if (originCountry != null) {
      submissionPayloadWithOriginCountry(submissionPayloadBuilder, originCountry);
    }
    if (consentToFederation != null) {
      submissionPayloadWithConsentToFederation(submissionPayloadBuilder, consentToFederation);
    }
    if (submissionType != null) {
      submissionPayloadWithSubmissionType(submissionPayloadBuilder, submissionType);
    }

    SubmissionPayload submissionPayload = submissionPayloadBuilder.build();
    return submissionPayload;
  }

  public static List<TemporaryExposureKey> createValidTemporaryExposureKeys(int rollingPeriod) {
    int numberOfKeys = 10;
    int transmissionRiskLevel = 6;
    ReportType reportType = ReportType.CONFIRMED_TEST;
    int daysSinceOnsetOfSymptoms = 0;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime todayMidnight = LocalDateTime
        .of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0);
    LocalDateTime todayMidnightMinusNumberOfKeys = todayMidnight.minusDays(numberOfKeys);

    return buildTemporaryExposureKeyWithoutMultiplying(numberOfKeys,
        todayMidnightMinusNumberOfKeys,
        transmissionRiskLevel, rollingPeriod, reportType, daysSinceOnsetOfSymptoms);
  }


  public static List<TemporaryExposureKey> createValidTemporaryExposureKeys() {
    int numberOfKeys = 10;
    int transmissionRiskLevel = 6;
    ReportType reportType = ReportType.CONFIRMED_TEST;
    int daysSinceOnsetOfSymptoms = 0;
    int rollingPeriod = 144;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime todayMidnight = LocalDateTime
        .of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0);
    LocalDateTime todayMidnightMinusNumberOfKeys = todayMidnight.minusDays(numberOfKeys);

    return buildTemporaryExposureKeys(numberOfKeys,
        todayMidnightMinusNumberOfKeys,
        transmissionRiskLevel, rollingPeriod, reportType, daysSinceOnsetOfSymptoms);
  }

  public static Builder submissionPayloadWithOriginCountry(Builder submissionPayload, String originCountry) {
    return submissionPayload.setOrigin(originCountry);
  }

  public static Builder submissionPayloadWithVisitedCountries(Builder submissionPayload,
      List<String> visitedCountries) {
    return submissionPayload.addAllVisitedCountries(visitedCountries);
  }

  public static Builder submissionPayloadWithConsentToFederation(Builder submissionPayload,
      boolean consentToFederation) {
    return submissionPayload.setConsentToFederation(consentToFederation);
  }

  public static Builder submissionPayloadWithSubmissionType(Builder submissionPayload, SubmissionType submissionType) {
    return submissionPayload.setSubmissionType(submissionType);
  }

}
