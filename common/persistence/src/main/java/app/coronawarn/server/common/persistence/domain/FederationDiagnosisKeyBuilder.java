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

package app.coronawarn.server.common.persistence.domain;

import static app.coronawarn.server.common.persistence.domain.FederationDiagnosisKeyBuilders.Builder;
import static app.coronawarn.server.common.persistence.domain.FederationDiagnosisKeyBuilders.FinalBuilder;
import static app.coronawarn.server.common.persistence.domain.FederationDiagnosisKeyBuilders.RollingStartIntervalNumberBuilder;
import static app.coronawarn.server.common.persistence.domain.FederationDiagnosisKeyBuilders.TransmissionRiskLevelBuilder;
import static app.coronawarn.server.common.persistence.domain.validation.ValidSubmissionTimestampValidator.SECONDS_PER_HOUR;

import app.coronawarn.server.common.persistence.exception.InvalidDiagnosisKeyException;
import app.coronawarn.server.common.protocols.external.exposurenotification.DiagnosisKey;
import app.coronawarn.server.common.protocols.external.exposurenotification.VerificationType;
import com.google.protobuf.ByteString;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this builder can be retrieved by calling {@link FederationDiagnosisKey#builder()}.
 * A {@link FederationDiagnosisKey} can then be build by either providing the required
 * member values or by passing the respective protocol buffer object.
 */
public class FederationDiagnosisKeyBuilder implements
    Builder, RollingStartIntervalNumberBuilder, TransmissionRiskLevelBuilder, FinalBuilder {

  private static final Logger logger = LoggerFactory.getLogger(FederationDiagnosisKeyBuilder.class);

  private byte[] keyData;
  private int rollingStartIntervalNumber;
  private int rollingPeriod = FederationDiagnosisKey.EXPECTED_ROLLING_PERIOD;
  private int transmissionRiskLevel;
  private Long submissionTimestamp = null;

  private List<String> visitedCountries;
  private String origin;
  private VerificationType verficationType;

  FederationDiagnosisKeyBuilder() {
  }

  @Override
  public RollingStartIntervalNumberBuilder withKeyData(byte[] keyData) {
    this.keyData = keyData;
    return this;
  }

  @Override
  public TransmissionRiskLevelBuilder withRollingStartIntervalNumber(int rollingStartIntervalNumber) {
    this.rollingStartIntervalNumber = rollingStartIntervalNumber;
    return this;
  }

  @Override
  public FinalBuilder withTransmissionRiskLevel(int transmissionRiskLevel) {
    this.transmissionRiskLevel = transmissionRiskLevel;
    return this;
  }

  @Override
  public FinalBuilder fromProtoBuf(DiagnosisKey protoBufObject) {
    return this
        .withKeyData(protoBufObject.getKeyData().toByteArray())
        .withRollingStartIntervalNumber(protoBufObject.getRollingStartIntervalNumber())
        .withTransmissionRiskLevel(protoBufObject.getTransmissionRiskLevel())
        .withRollingPeriod(protoBufObject.getRollingPeriod())
        .withVisitedCountries(protoBufObject.getVisitedCountriesList())
        .withOrigin(protoBufObject.getOrigin())
        .withVerificationType(protoBufObject.getVerificationType());
  }

  @Override
  public FinalBuilder withSubmissionTimestamp(long submissionTimestamp) {
    this.submissionTimestamp = submissionTimestamp;
    return this;
  }

  @Override
  public FinalBuilder withRollingPeriod(int rollingPeriod) {
    this.rollingPeriod = rollingPeriod;
    return this;
  }

  @Override
  public FinalBuilder withVisitedCountries(List<String> visitedCountries) {
    this.visitedCountries = visitedCountries;
    return this;
  }

  @Override
  public FinalBuilder withOrigin(String origin) {
    this.origin = origin;
    return this;
  }

  @Override
  public FinalBuilder withVerificationType(VerificationType verficationType) {
    this.verficationType = verficationType;
    return this;
  }

  @Override
  public FederationDiagnosisKey build() {
    if (submissionTimestamp == null) {
      // hours since epoch
      submissionTimestamp = Instant.now().getEpochSecond() / SECONDS_PER_HOUR;
    }

    var federationDiagnosisKey = new FederationDiagnosisKey(
        keyData, rollingStartIntervalNumber, rollingPeriod, transmissionRiskLevel, submissionTimestamp,
        visitedCountries, origin, verficationType);
    return throwIfValidationFails(federationDiagnosisKey);
  }

  private FederationDiagnosisKey throwIfValidationFails(FederationDiagnosisKey federationDiagnosisKey) {
    Set<ConstraintViolation<FederationDiagnosisKey>> violations = federationDiagnosisKey.validate();

    if (!violations.isEmpty()) {
      String violationsMessage = violations.stream()
          .map(violation -> String.format("%s Invalid Value: %s", violation.getMessage(), violation.getInvalidValue()))
          .collect(Collectors.toList()).toString();
      logger.debug(violationsMessage);
      throw new InvalidDiagnosisKeyException(violationsMessage);
    }

    return federationDiagnosisKey;
  }
}
