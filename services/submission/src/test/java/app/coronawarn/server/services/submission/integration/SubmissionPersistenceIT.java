

package app.coronawarn.server.services.submission.integration;

import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildSubmissionPayload;
import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildTemporaryExposureKeys;
import static app.coronawarn.server.services.submission.assertions.SubmissionAssertions.assertElementsCorrespondToEachOther;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.Builder;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.controller.SubmissionController;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
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
  public static final String PATH_MOBILE_CLIENT_PAYLOAD_PB = "src/test/resources/payload/mobile-client-payload.pb";

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionController submissionController;

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
  @ValueSource(strings = {PATH_MOBILE_CLIENT_PAYLOAD_PB})
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
      Boolean consentToFederation) throws IOException {

    List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();

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

    SubmissionPayload submissionPayload = submissionPayloadBuilder.build();

    writeSubmissionPayloadProtobufFile(submissionPayload);

    Path path = Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB);
    InputStream input = new FileInputStream(path.toFile());
    SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    logger.info("Submitting payload: " + System.lineSeparator()
        + JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(payload));

    executor.executePost(payload);

    String presenceVerificationSql = generateDebugSqlStatement(payload);
    logger.info("SQL debugging statement: " + System.lineSeparator() + presenceVerificationSql);
    Integer result = jdbcTemplate.queryForObject(presenceVerificationSql, Integer.class);

    List<String> expectedVisitedCountries = new ArrayList<>(payload.getVisitedCountriesList());
    expectedVisitedCountries.add(StringUtils
        .defaultIfBlank(payload.getOrigin(), config.getDefaultOriginCountry()));

    SubmissionPayload expectedPayload = SubmissionPayload.newBuilder()
        .addAllKeys(payload.getKeysList())
        .setRequestPadding(payload.getRequestPadding())
        .addAllVisitedCountries(expectedVisitedCountries)
        .setOrigin(StringUtils.defaultIfBlank(payload.getOrigin(), config.getDefaultOriginCountry()))
        .setConsentToFederation(payload.getConsentToFederation())
        .build();

    assertEquals(payload.getKeysList().size(), result);

    assertElementsCorrespondToEachOther(expectedPayload, diagnosisKeyService.getDiagnosisKeys(), config);
  }

  private static Stream<Arguments> validSubmissionPayload() {
    return Stream.of(
        Arguments.of(null, null, null),
        Arguments.of(List.of("DE"), null, true),
        Arguments.of(List.of("DE"), null, false),
        Arguments.of(List.of("DE"), null, null),
        Arguments.of(null, "DE", true),
        Arguments.of(null, "DE", false),
        Arguments.of(null, "DE", null),
        Arguments.of(List.of("DE"), "DE", true),
        Arguments.of(List.of("DE"), "DE", false),
        Arguments.of(List.of("DE"), "DE", null),
        Arguments.of(List.of("DE", "IT"), "DE", true),
        Arguments.of(List.of("DE", "IT"), "DE", false),
        Arguments.of(List.of("DE", "IT"), "DE", null),
        Arguments.of(List.of("DE"), "IT", true),
        Arguments.of(List.of("DE"), "IT", false),
        Arguments.of(List.of("DE"), "IT", null),
        Arguments.of(List.of("IT"), "DE", true),
        Arguments.of(List.of("IT"), "DE", false),
        Arguments.of(List.of("IT"), "DE", null),
        Arguments.of(List.of("IT"), "IT", true),
        Arguments.of(List.of("IT"), "IT", false),
        Arguments.of(List.of("IT"), "IT", null),
        Arguments.of(List.of("IT", "DE"), "IT", false),
        Arguments.of(List.of("DE"), "DE", null),
        Arguments.of(List.of("DE"), "IT", null),
        Arguments.of(List.of("DE"), "IT", true),
        Arguments.of(List.of("DE"), "IT", false),
        Arguments.of(List.of("IT"), "DE", null)
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
        Arguments.of(List.of("IT"), "", null),
        Arguments.of(List.of("IT"), "", true),
        Arguments.of(List.of("IT"), "", false)
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

  private void writeSubmissionPayloadProtobufFile(SubmissionPayload submissionPayload) throws IOException {
    File file = new File(PATH_MOBILE_CLIENT_PAYLOAD_PB);
    file.createNewFile();
    submissionPayload
        .writeTo(new FileOutputStream(PATH_MOBILE_CLIENT_PAYLOAD_PB));
  }

  private List<TemporaryExposureKey> createValidTemporaryExposureKeys() {
    int numberOfKeys = 10;
    int transmissionRiskLevel = 6;
    int rollingPeriod = 144; // 24*60/10
    ReportType reportType = ReportType.CONFIRMED_CLINICAL_DIAGNOSIS;
    int daysSinceOnsetOfSymptoms = 0;

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime todayMidnight = LocalDateTime
        .of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0);
    LocalDateTime todayMidnightMinusNumberOfKeys = todayMidnight.minusDays(numberOfKeys);

    return buildTemporaryExposureKeys(numberOfKeys,
        todayMidnightMinusNumberOfKeys,
        transmissionRiskLevel, rollingPeriod, reportType, daysSinceOnsetOfSymptoms);
  }

  private Builder submissionPayloadWithOriginCountry(Builder submissionPayload, String originCountry) {
    return submissionPayload
        .setOrigin(originCountry);
  }

  private Builder submissionPayloadWithVisitedCountries(Builder submissionPayload, List<String> visitedCountries) {
    return submissionPayload
        .addAllVisitedCountries(visitedCountries);
  }

  private Builder submissionPayloadWithConsentToFederation(Builder submissionPayload, boolean consentToFederation) {
    return submissionPayload
        .setConsentToFederation(consentToFederation);
  }
}
