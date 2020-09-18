

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.createRollingStartIntervalNumber;


import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SubmissionPayloadMockData {

  public static SubmissionPayload buildPayload(TemporaryExposureKey key) {
    Collection<TemporaryExposureKey> keys = Stream.of(key).collect(Collectors.toCollection(ArrayList::new));
    return buildPayload(keys);
  }

  public static SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE")
        .build();
  }

  public static SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys, boolean consentToFederation) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE")
        .setConsentToFederation(consentToFederation)
        .build();
  }

  public static SubmissionPayload buildInvalidPayload(TemporaryExposureKey key) {
    Collection<TemporaryExposureKey> keys = Stream.of(key).collect(Collectors.toCollection(ArrayList::new));
    return buildInvalidPayload(keys);
  }

  public static SubmissionPayload buildInvalidPayload(Collection<TemporaryExposureKey> keys) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE3")
        .build();
  }

  public static SubmissionPayload buildPayloadWithPadding(Collection<TemporaryExposureKey> keys) {
    return buildPayloadWithPadding(keys, "PaddingString".getBytes());
  }

  public static SubmissionPayload buildPayloadWithTooLargePadding(SubmissionServiceConfig config,
                                                                  Collection<TemporaryExposureKey> keys) {
    int exceedingSize = (int) (2 * config.getMaximumRequestSize().toBytes());
    byte[] bytes = new byte[exceedingSize];
    return buildPayloadWithPadding(keys, bytes);
  }

  private static SubmissionPayload buildPayloadWithPadding(Collection<TemporaryExposureKey> keys, byte[] bytes) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addAllVisitedCountries(List.of("FR"))
        .setOrigin("DE")
        .setRequestPadding(ByteString.copyFrom(bytes))
        .build();
  }

  public static SubmissionPayload buildPayloadWithInvalidKey() {
    TemporaryExposureKey invalidKey =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 999, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);
    return buildPayload(invalidKey);
  }

  public static SubmissionPayload buildPayloadWithInvalidOriginCountry() {
    TemporaryExposureKey key =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 2, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);
    return buildInvalidPayload(key);
  }

  public static SubmissionPayload buildPayloadWithVisitedCountries(List<String> visitedCountries) {
    TemporaryExposureKey key =
        buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 2, ReportType.CONFIRMED_CLINICAL_DIAGNOSIS, 1);
    return SubmissionPayload.newBuilder()
        .addKeys(key)
        .addAllVisitedCountries(visitedCountries)
        .setOrigin("DE")
        .setRequestPadding(ByteString.copyFrom("PaddingString".getBytes()))
        .build();
  }

}
