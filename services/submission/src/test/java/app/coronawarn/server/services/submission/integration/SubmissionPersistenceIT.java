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

package app.coronawarn.server.services.submission.integration;

import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildSubmissionPayload;
import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildTemporaryExposureKeys;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static app.coronawarn.server.services.submission.assertions.SubmissionAssertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.services.submission.SubmissionPayloadGenerator;
import com.google.protobuf.ByteString;
import org.apache.tomcat.util.buf.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.util.JsonFormat;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.TanVerifier;

/**
 * This test serves more like a dev tool which helps with debugging production issues. It inserts keys parsed from a
 * proto buf file whos content was captured by the mobile client during requests to the server. The content of the
 * current test resource file can be quickly replaced during the investigation of an issue.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"disable-ssl-client-verification", "disable-ssl-client-verification-verify-hostname"})
@Sql(scripts = {"classpath:db/clean_db_state.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class SubmissionPersistenceIT {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionPersistenceIT.class);

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @MockBean
  private TanVerifier tanVerifier;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  @Disabled("Because the content of the .pb file becomes old and retention time passes, this test will fail. "
      + "Enable when debugging of a new payload is required.")
  @ParameterizedTest
  @ValueSource(strings = {"src/test/resources/payload/mobile-client-payload.pb"})
  void testKeyInsertionWithMobileClientProtoBuf(String testFile) throws IOException {
    Path path = Paths.get(testFile);
    InputStream input = new FileInputStream(path.toFile());
    SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    logger.info("Submitting payload: " + System.lineSeparator()
        + JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(payload));

    executor.executePost(payload);

    String presenceVerificationSql = generateDebugSqlStatement(payload);
    logger.info("SQL debugging statement: " + System.lineSeparator() + presenceVerificationSql);
    Integer result = jdbcTemplate.queryForObject(presenceVerificationSql, Integer.class);

    assertEquals(payload.getKeysList().size(), result);
    assertElementsCorrespondToEachOther(payload, diagnosisKeyService.getDiagnosisKeys(), config);
  }

  @ParameterizedTest
  @MethodSource("validSubmissionPayload")
  void testKeyInsertionWithMobileClientProtoBuf(List<String> visitedCountries, String originCountry,
      boolean consentToFederation) throws IOException {
    int numberOfKeys = 10;
    int transmissionRiskLevel = 6;
    int rollingPeriod = 144; // 24*60/10
    ReportType reportType = ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;
    ByteString requestPadding = ByteString.copyFrom(new byte[100]);
    int daysSinceOnsetOfSymptoms = 0;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime todayMidnight = LocalDateTime
        .of(now.getYear(), now.getMonth(), now.getDayOfMonth() , 0, 0);
    LocalDateTime todayMidnightMinusNumberOfKeys = todayMidnight.minusDays(numberOfKeys);

    List<TemporaryExposureKey> temporaryExposureKeys = buildTemporaryExposureKeys(numberOfKeys, todayMidnightMinusNumberOfKeys,
        transmissionRiskLevel, rollingPeriod, reportType, daysSinceOnsetOfSymptoms);
    SubmissionPayload submissionPayload = buildSubmissionPayload(temporaryExposureKeys, requestPadding,
        visitedCountries, originCountry, consentToFederation);

    SubmissionPayloadGenerator submissionPayloadGenerator = new SubmissionPayloadGenerator();
    submissionPayloadGenerator.writeSubmissionPayloadProtobufFile(submissionPayload);

    Path path = Paths.get("src/test/resources/payload/mobile-client-payload.pb");
    InputStream input = new FileInputStream(path.toFile());
    SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    logger.info("Submitting payload: " + System.lineSeparator()
        + JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(payload));

    executor.executePost(payload);

    String presenceVerificationSql = generateDebugSqlStatement(payload);
    logger.info("SQL debugging statement: " + System.lineSeparator() + presenceVerificationSql);
    Integer result = jdbcTemplate.queryForObject(presenceVerificationSql, Integer.class);

    assertEquals(payload.getKeysList().size(), result);
    assertElementsCorrespondToEachOther(payload, diagnosisKeyService.getDiagnosisKeys(), config);
  }

  private static Stream<Arguments> validSubmissionPayload() {
    return Stream.of(
        Arguments.of(List.of("DE"), "DE", true),
        Arguments.of(List.of("DE"), "DE", false),
        Arguments.of(List.of("DE", "IT"), "DE", true),
        Arguments.of(List.of("DE", "IT"), "DE", false),
        Arguments.of(List.of("DE"), "IT", true),
        Arguments.of(List.of("DE"), "IT", false),
        Arguments.of(List.of("IT"), "", true),
        Arguments.of(List.of("IT"), "", false),
        Arguments.of(List.of("IT"), "DE", true),
        Arguments.of(List.of("IT"), "DE", false),
        Arguments.of(List.of("IT"), "IT", true),
        Arguments.of(List.of("IT", "DE"), "IT", false)
    );
  }

  private static Stream<Arguments> invalidSubmissionPayload() {
    return Stream.of(
        Arguments.of(List.of(""), "", null),
        Arguments.of(List.of(""), "", true),
        Arguments.of(List.of(""), "", false),
        Arguments.of(List.of(""), "DE", null),
        Arguments.of(List.of(""), "DE", true),
        Arguments.of(List.of(""), "DE", false),
        Arguments.of(List.of(""), "IT", null),
        Arguments.of(List.of(""), "IT", true),
        Arguments.of(List.of(""), "IT", false),
        Arguments.of(List.of("DE"), "", null),
        Arguments.of(List.of("DE"), "", true),
        Arguments.of(List.of("DE"), "", false),
        Arguments.of(List.of("DE"), "DE", null),
        Arguments.of(List.of("DE,IT"), "DE", null),
        Arguments.of(List.of("DE"), "IT", null),
        Arguments.of(List.of("DE"), "IT", true),
        Arguments.of(List.of("DE"), "IT", false),
        Arguments.of(List.of("IT"), "", null),
        Arguments.of(List.of("IT"), "", true),
        Arguments.of(List.of("IT"), "", false),
        Arguments.of(List.of("IT"), "DE", null),
        Arguments.of(List.of("IT"), "IT", null)
    );
  }

  private String generateDebugSqlStatement(SubmissionPayload payload) {
    List<String> base64Keys = payload.getKeysList()
        .stream()
        .map(key -> "'" + toBase64(key) + "'")
        .collect(Collectors.toList());
    return "SELECT count(*) FROM diagnosis_key where ENCODE(key_data, 'BASE64') IN ("
        + StringUtils.join(base64Keys, ',') + ")";
  }

  private String toBase64(TemporaryExposureKey key) {
    return BaseEncoding.base64().encode((key.getKeyData()).toByteArray());
  }
}
