/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.submission.controller;

import static app.coronawarn.server.services.submission.controller.RequestExecutor.VALID_KEY_DATA_1;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.buildTemporaryExposureKey;
import static app.coronawarn.server.services.submission.controller.RequestExecutor.createRollingStartIntervalNumber;


import app.coronawarn.server.common.protocols.external.exposurenotification.VerificationType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.protobuf.ByteString;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;

public final class SubmissionPayloadMockData {

  public static SubmissionPayload buildPayload(TemporaryExposureKey key) {
    Collection<TemporaryExposureKey> keys = Stream.of(key).collect(Collectors.toCollection(ArrayList::new));
    return buildPayload(keys);
  }

  public static SubmissionPayload buildPayload(Collection<TemporaryExposureKey> keys) {
    return SubmissionPayload.newBuilder()
        .addAllKeys(keys)
        .addVisitedCountries("DE")
        .addVisitedCountries("FR")
        .setOrigin("DE")
        .setVerificationType(VerificationType.LAB_VERIFIED)
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
        .setPadding(ByteString.copyFrom(bytes))
        .addVisitedCountries("DE")
        .addVisitedCountries("FR")
        .setOrigin("DE")
        .setVerificationType(VerificationType.LAB_VERIFIED)
        .build();
  }

  public static SubmissionPayload buildPayloadWithInvalidKey() {
    TemporaryExposureKey invalidKey = buildTemporaryExposureKey(VALID_KEY_DATA_1, createRollingStartIntervalNumber(2), 999);
    return buildPayload(invalidKey);
  }

  public static SubmissionPayload buildPayloadWithInvalidOriginCountry() {
    //TODO Implement this once submission payload proto is defined
    return null;
  }

  public static SubmissionPayload buildPayloadWithInvalidVisitedCountries() {
    //TODO Implement this once submission payload proto is defined
    return null;
  }

}
