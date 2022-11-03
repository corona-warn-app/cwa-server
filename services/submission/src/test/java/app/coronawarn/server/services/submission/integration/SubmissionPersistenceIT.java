package app.coronawarn.server.services.submission.integration;

import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_HOST_WARNING;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_PCR_TEST;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_SRS_RAPID_PCR;
import static app.coronawarn.server.services.submission.assertions.SubmissionAssertions.assertElementsCorrespondToEachOther;
import static app.coronawarn.server.services.submission.controller.SubmissionController.SUBMISSION_ON_BEHALF_ROUTE;
import static app.coronawarn.server.services.submission.controller.SubmissionController.SUBMISSION_ROUTE;
import static app.coronawarn.server.services.submission.integration.DataHelpers.buildDefaultCheckIn;
import static app.coronawarn.server.services.submission.integration.DataHelpers.buildDefaultEncryptedCheckIn;
import static app.coronawarn.server.services.submission.integration.DataHelpers.buildSubmissionPayload;
import static app.coronawarn.server.services.submission.integration.DataHelpers.buildSubmissionPayloadWithCheckins;
import static app.coronawarn.server.services.submission.integration.DataHelpers.createValidTemporaryExposureKeys;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.common.protocols.internal.pt.CheckIn;
import app.coronawarn.server.common.protocols.internal.pt.CheckInProtectedReport;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
import app.coronawarn.server.services.submission.verification.EventTanVerifier;
import app.coronawarn.server.services.submission.verification.SrsOtpVerifier;
import app.coronawarn.server.services.submission.verification.TanVerifier;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.util.JsonFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("integration-test")
@Sql(scripts = { "classpath:db/clean_db_state.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class SubmissionPersistenceIT {

  private static final Logger logger = LoggerFactory.getLogger(SubmissionPersistenceIT.class);
  public static final String PATH_MOBILE_CLIENT_PAYLOAD_PB = "src/test/resources/payload";
  public static final String FILENAME_MOBILE_CLIENT_PAYLOAD_PB = "mobile-client-payload.pb";
  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  DiagnosisKeyRepository diagnosisKeyRepository;

  @MockBean
  private TanVerifier tanVerifier;

  @MockBean
  private SrsOtpVerifier srsOtpVerifier;

  @MockBean
  private EventTanVerifier eventTanVerifier;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(srsOtpVerifier.verifyTan(anyString())).thenReturn(true);
    when(eventTanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  @Test
  void testDiagnosisKeyRollingPeriodIsZeroShouldNotBeSaved() {

    // given
    List<TemporaryExposureKey> invalidKeys = createValidTemporaryExposureKeys(0);

    List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);

    SubmissionPayload invalidSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        invalidKeys, SUBMISSION_TYPE_PCR_TEST);
    String tan = "tan";
    final HttpHeaders headers = headers();
    headers.add("cwa-authorization", tan);

    SubmissionPayload validSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        validKeys, SUBMISSION_TYPE_PCR_TEST);

    testRestTemplate
        .postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(invalidSubmissionPayload, headers),
            Void.class);
    assertEquals(0, diagnosisKeyRepository.count());

    testRestTemplate
        .postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(validSubmissionPayload, headers), Void.class);

    final int expectedNumberOfKeys =
        validSubmissionPayload.getKeysList().size() * config.getRandomKeyPaddingMultiplier();
    assertEquals(expectedNumberOfKeys, diagnosisKeyRepository.count());
  }

  /**
   * Test when (cwa-authorization && cwa-otp) are missing.
   */
  @Test
  void testBadRequest0() {
    final List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);
    final SubmissionPayload validSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        validKeys, SUBMISSION_TYPE_SRS_RAPID_PCR);

    final ResponseEntity<Void> response = testRestTemplate
        .postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(validSubmissionPayload, headers()),
            Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  /**
   * Test when (cwa-authorization && cwa-otp) are both provided.
   */
  @Test
  void testBadRequest2() {
    final List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);
    final HttpHeaders headers = headers();

    headers.add("cwa-authorization", "foo");
    headers.add("cwa-otp", "bar");

    final SubmissionPayload validSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        validKeys, SUBMISSION_TYPE_SRS_RAPID_PCR);

    final ResponseEntity<Void> response = testRestTemplate
        .postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(validSubmissionPayload, headers), Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  private HttpHeaders headers() {
    final HttpHeaders headers = new HttpHeaders();
    headers.add("cwa-fake", "0");
    headers.setContentType(MediaType.valueOf("Application/x-protobuf"));
    return headers;
  }

  /**
   * Test Self-Report Submission (SRS).
   * 
   * @param submissionTypeNumber - {@link SubmissionType}
   */
  @ParameterizedTest
  @ValueSource(ints = { 3, 4, 5, 6, 7, 8 }) // SUBMISSION_TYPE_SRS_*
  void testSrsOtpAuth(int submissionTypeNumber) {
    final List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);
    final HttpHeaders headers = headers();
    headers.add("cwa-otp", "bar");

    final SubmissionPayload validSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        validKeys, SubmissionType.forNumber(submissionTypeNumber));

    final int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM self_report_submissions", Integer.class);

    final ResponseEntity<Void> response = testRestTemplate
        .postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(validSubmissionPayload, headers),
            Void.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    final int result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM self_report_submissions", Integer.class);
    assertEquals(before + 1, result);
  }

  @Test
  @SuppressWarnings("deprecation")
  void testSubmissionOnBehalf() {
    byte[] locationIdHash = new byte[32];
    new Random().nextBytes(locationIdHash);
    List<CheckInProtectedReport> protectedReports = List.of(DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash),
        DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash));
    List<CheckIn> checkIns = List.of(DataHelpers.buildDefaultCheckIn(), DataHelpers.buildDefaultCheckIn());
    SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder().addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList()).setConsentToFederation(false)
        .setSubmissionType(SUBMISSION_TYPE_HOST_WARNING).addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns).build();
    final HttpHeaders headers = headers();
    headers.add("cwa-authorization", "is mocked and returns true :-)");

    final int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM check_in_protected_reports", Integer.class);
    final int none = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM diagnosis_key", Integer.class);

    final ResponseEntity<Void> response = testRestTemplate.postForEntity("/version/v1" + SUBMISSION_ON_BEHALF_ROUTE,
        new HttpEntity<>(validSubmissionPayload, headers), Void.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    final int result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM check_in_protected_reports", Integer.class);
    assertEquals(before + 2, result);
    assertEquals(none, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM diagnosis_key", Integer.class));
  }

  @ParameterizedTest
  @ValueSource(strings = { PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB })
  void testKeyInsertionWithMobileClientProtoBuf(String testFile) throws IOException {
    List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();
    SubmissionPayload submissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        temporaryExposureKeys, SubmissionType.SUBMISSION_TYPE_PCR_TEST);

    writeSubmissionPayloadProtobufFile(submissionPayload);

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
  void okKeyInsertionWithMobileClientProtoBuf(List<String> visitedCountries, String originCountry,
      Boolean consentToFederation, SubmissionType submissionType) throws IOException {

    List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();

    SubmissionPayload submissionPayload = buildSubmissionPayload(visitedCountries, originCountry, consentToFederation,
        temporaryExposureKeys, submissionType);

    writeSubmissionPayloadProtobufFile(submissionPayload);

    Path path = Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
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
        .setSubmissionType(submissionType)
        .build();

    assertEquals(payload.getKeysList().size(), result);
    assertElementsCorrespondToEachOther(expectedPayload, diagnosisKeyService.getDiagnosisKeys(), config);
  }

  @Test
  public void defaultsToPcrIfSubmissionTypeIsUndefined() throws IOException {
    List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();

    SubmissionPayload submissionPayload = buildSubmissionPayload(emptyList(), "DE", true, temporaryExposureKeys, null);

    writeSubmissionPayloadProtobufFile(submissionPayload);

    Path path = Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
    InputStream input = new FileInputStream(path.toFile());
    SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    logger.info("Submitting payload: " + System.lineSeparator()
        + JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(payload));

    executor.executePost(payload);

    assertTrue(diagnosisKeyService.getDiagnosisKeys().stream()
        .allMatch(diagnosisKey -> diagnosisKey.getSubmissionType().equals(SubmissionType.SUBMISSION_TYPE_PCR_TEST)));
  }

  private static Stream<Arguments> validSubmissionPayload() {
    return Stream.of(
        Arguments.of(null, null, null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, null, null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, null, true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, null, true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, null, false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, null, false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, "DE", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, "DE", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, "DE", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, "DE", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, "DE", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, "DE", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "DE", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "DE", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "DE", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "DE", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "DE", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "DE", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), null, false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), null, false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "DE", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "DE", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "DE", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "DE", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "DE", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "DE", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), null, null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), null, null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), null, true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), null, true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), null, false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), null, false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "DE", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "DE", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "DE", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "DE", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "DE", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "DE", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), null, null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), null, null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), null, true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), null, true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), null, false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), null, false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", true, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", true, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", false, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", false, SubmissionType.SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", null, SubmissionType.SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", null, SubmissionType.SUBMISSION_TYPE_RAPID_TEST)
    );
  }

  @ParameterizedTest
  @MethodSource("invalidSubmissionPayload")
  void failKeyInsertionWithMobileClientProtoBuf(List<String> visitedCountries, String originCountry,
      Boolean consentToFederation) throws IOException {

    List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();

    SubmissionPayload submissionPayload = buildSubmissionPayload(visitedCountries, originCountry, consentToFederation,
        temporaryExposureKeys, SubmissionType.SUBMISSION_TYPE_PCR_TEST);

    writeSubmissionPayloadProtobufFile(submissionPayload);

    Path path = Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
    InputStream input = new FileInputStream(path.toFile());
    SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    executor.executePost(payload);

    assertEquals(0, diagnosisKeyService.getDiagnosisKeys().size());
  }

  private static Stream<Arguments> invalidSubmissionPayload() {
    return Stream.of(
        Arguments.of(List.of("XX"), null, null),
        Arguments.of(List.of("XX"), null, true),
        Arguments.of(List.of("XX"), null, false),
        Arguments.of(List.of("XX"), "DE", true),
        Arguments.of(List.of("XX"), "DE", false),
        Arguments.of(List.of("XX"), "DE", null),
        Arguments.of(List.of("XX"), "IT", true),
        Arguments.of(List.of("XX"), "IT", false),
        Arguments.of(List.of("XX"), "IT", null),
        Arguments.of(List.of("DE", "XX"), null, null),
        Arguments.of(List.of("DE", "XX"), null, true),
        Arguments.of(List.of("DE", "XX"), null, false),
        Arguments.of(List.of("DE", "XX"), "DE", true),
        Arguments.of(List.of("DE", "XX"), "DE", false),
        Arguments.of(List.of("DE", "XX"), "DE", null),
        Arguments.of(List.of("DE", "XX"), "IT", true),
        Arguments.of(List.of("DE", "XX"), "IT", false),
        Arguments.of(List.of("DE", "XX"), "IT", null),
        Arguments.of(List.of(""), "", null),
        Arguments.of(List.of(""), "", true),
        Arguments.of(List.of(""), "", false),
        Arguments.of(List.of(""), "DE", null),
        Arguments.of(List.of(""), "DE", true),
        Arguments.of(List.of(""), "DE", false),
        Arguments.of(List.of(""), "IT", null),
        Arguments.of(List.of(""), "IT", true),
        Arguments.of(List.of(""), "IT", false),
        Arguments.of(null, "RU", null),
        Arguments.of(null, "RU", true),
        Arguments.of(null, "RU", false),
        Arguments.of(List.of("RU"), null, null),
        Arguments.of(List.of("RU"), null, true),
        Arguments.of(List.of("RU"), null, false),
        Arguments.of(List.of("RU"), "RU", true),
        Arguments.of(List.of("RU"), "RU", false),
        Arguments.of(List.of("RU"), "RU", null),
        Arguments.of(List.of("IT"), "RU", true),
        Arguments.of(List.of("IT"), "RU", false),
        Arguments.of(List.of("IT"), "RU", null),
        Arguments.of(List.of("RU"), "IT", true),
        Arguments.of(List.of("RU"), "IT", false),
        Arguments.of(List.of("RU"), "IT", null)
    );
  }

  @Deprecated
  @Test
  void unencryptedCheckInsEnabledShouldResultInSavingCorrectNumberOfCheckIns() {
    // GIVEN:
    List<String> visitedCountries = Collections.singletonList("DE");
    String originCountry = "DE";

    List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();
    List<CheckInProtectedReport> protectedReports = Collections.singletonList(buildDefaultEncryptedCheckIn());
    List<CheckIn> checkins = Collections.singletonList(
        buildDefaultCheckIn());
    SubmissionPayload submissionPayload = buildSubmissionPayloadWithCheckins(visitedCountries, originCountry,
        true,
        temporaryExposureKeys, SubmissionType.SUBMISSION_TYPE_PCR_TEST, protectedReports, checkins);

    // WHEN:
    ResponseEntity<Void> result = executor.executePost(submissionPayload);

    // THEN:
    // For the one valid unencrypted checkin we generate one fake checkins and we also save the encrypted checkins
    // which is 1+(1 Fake)+1 = 3 Saved checkins
    assertThat(result.getHeaders().get("cwa-filtered-checkins").get(0)).isEqualTo("0");
    assertThat(result.getHeaders().get("cwa-saved-checkins").get(0)).isEqualTo("3");
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
    Files.createDirectories(Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB));
    File file = new File(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
    file.createNewFile();
    submissionPayload
        .writeTo(new FileOutputStream(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB));
  }
}
