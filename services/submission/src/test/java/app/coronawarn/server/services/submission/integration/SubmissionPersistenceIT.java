package app.coronawarn.server.services.submission.integration;

import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_HOST_WARNING;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_PCR_TEST;
import static app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType.SUBMISSION_TYPE_RAPID_TEST;
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
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
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
        Arguments.of(List.of("RU"), "IT", null));
  }

  private static Stream<Arguments> validSubmissionPayload() {
    return Stream.of(
        Arguments.of(null, null, null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, null, null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, null, true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, null, true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, null, false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, null, false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, "DE", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, "DE", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, "DE", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, "DE", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(null, "DE", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(null, "DE", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "DE", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "DE", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "DE", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "DE", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(emptyList(), "DE", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(emptyList(), "DE", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), null, false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), null, false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "DE", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "DE", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "DE", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "DE", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE"), "DE", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE"), "DE", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), null, null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), null, null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), null, true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), null, true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), null, false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), null, false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "DE", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "DE", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "DE", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "DE", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("IT"), "DE", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("IT"), "DE", null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), null, null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), null, null, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), null, true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), null, true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), null, false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), null, false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", true, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", true, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", false, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", false, SUBMISSION_TYPE_RAPID_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", null, SUBMISSION_TYPE_PCR_TEST),
        Arguments.of(List.of("DE", "IT"), "DE", null, SUBMISSION_TYPE_RAPID_TEST));
  }

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Autowired
  private RequestExecutor executor;

  @Autowired
  private SubmissionServiceConfig config;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  private DiagnosisKeyRepository diagnosisKeyRepository;

  @MockBean
  private TanVerifier tanVerifier;

  @MockBean
  private SrsOtpVerifier srsOtpVerifier;

  @MockBean
  private EventTanVerifier eventTanVerifier;

  @MockBean
  private FakeDelayManager fakeDelayManager;

  @Test
  public void defaultsToPcrIfSubmissionTypeIsUndefined() throws IOException {
    final List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();

    final var payload1 = buildSubmissionPayload(emptyList(), "DE", true, temporaryExposureKeys, null);

    writeSubmissionPayloadProtobufFile(payload1);

    final Path path = Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
    final InputStream input = new FileInputStream(path.toFile());
    final SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    logger.info("Submitting payload: " + System.lineSeparator()
        + JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(payload));

    executor.executePost(payload);

    assertTrue(diagnosisKeyService.getDiagnosisKeys().stream()
        .allMatch(diagnosisKey -> SUBMISSION_TYPE_PCR_TEST.equals(diagnosisKey.getSubmissionType())));
  }

  @ParameterizedTest
  @MethodSource("invalidSubmissionPayload")
  void failKeyInsertionWithMobileClientProtoBuf(final List<String> visitedCountries, final String originCountry,
      final Boolean consentToFederation) throws IOException {

    final List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();

    final SubmissionPayload submissionPayload = buildSubmissionPayload(visitedCountries, originCountry,
        consentToFederation, temporaryExposureKeys, SUBMISSION_TYPE_PCR_TEST);

    writeSubmissionPayloadProtobufFile(submissionPayload);

    final Path path = Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
    final InputStream input = new FileInputStream(path.toFile());
    final SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    executor.executePost(payload);

    assertEquals(0, diagnosisKeyService.getDiagnosisKeys().size());
  }

  private String generateDebugSqlStatement(final SubmissionPayload payload) {
    final List<String> base64Keys = payload.getKeysList()
        .stream()
        .map(key -> "'" + toBase64(key) + "'")
        .toList();
    return "SELECT count(*) FROM diagnosis_key where ENCODE(key_data, 'BASE64') IN ("
        + StringUtils.join(base64Keys, ',') + ")";
  }

  private HttpHeaders headers() {
    final HttpHeaders headers = new HttpHeaders();
    headers.add("cwa-fake", "0");
    headers.setContentType(MediaType.valueOf("Application/x-protobuf"));
    return headers;
  }

  @ParameterizedTest
  @MethodSource("validSubmissionPayload")
  void okKeyInsertionWithMobileClientProtoBuf(final List<String> visitedCountries, final String originCountry,
      final Boolean consentToFederation, final SubmissionType submissionType) throws IOException {

    final List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();

    final SubmissionPayload submissionPayload = buildSubmissionPayload(visitedCountries, originCountry,
        consentToFederation, temporaryExposureKeys, submissionType);

    writeSubmissionPayloadProtobufFile(submissionPayload);

    final Path path = Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
    final InputStream input = new FileInputStream(path.toFile());
    final SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    logger.info("Submitting payload: " + System.lineSeparator()
        + JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(payload));

    executor.executePost(payload);

    final String presenceVerificationSql = generateDebugSqlStatement(payload);
    logger.info("SQL debugging statement: " + System.lineSeparator() + presenceVerificationSql);
    final Integer result = jdbcTemplate.queryForObject(presenceVerificationSql, Integer.class);

    final List<String> expectedVisitedCountries = new ArrayList<>(payload.getVisitedCountriesList());
    expectedVisitedCountries.add(StringUtils
        .defaultIfBlank(payload.getOrigin(), config.getDefaultOriginCountry()));

    final SubmissionPayload expectedPayload = SubmissionPayload.newBuilder()
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

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(srsOtpVerifier.verifyTan(anyString())).thenReturn(true);
    when(eventTanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  /**
   * Test when (cwa-authorization && cwa-otp) are missing.
   */
  @Test
  void testBadRequest0() {
    final List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);
    final var payload = buildSubmissionPayload(List.of("DE"), "DE", true, validKeys, SUBMISSION_TYPE_SRS_RAPID_PCR);

    final var response = rest.postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(payload, headers()),
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

    final var payload = buildSubmissionPayload(List.of("DE"), "DE", true, validKeys, SUBMISSION_TYPE_SRS_RAPID_PCR);

    final var response = rest.postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(payload, headers),
        Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, names = { "(?!SUBMISSION_TYPE_SRS_).*" }, mode = Mode.MATCH_ANY)
  void testBadRequest3(final SubmissionType type) {
    final List<TemporaryExposureKey> keys = createValidTemporaryExposureKeys(1);
    final HttpHeaders headers = headers();
    headers.add("cwa-otp", "bar");

    final SubmissionPayload payload = buildSubmissionPayload(List.of("DE"), "DE", true, keys, type);

    final ResponseEntity<Void> response = rest.postForEntity("/version/v1" + SUBMISSION_ROUTE,
        new HttpEntity<>(payload, headers), Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, names = { "SUBMISSION_TYPE_SRS_.*" }, mode = Mode.MATCH_ANY)
  void testBadRequest4(final SubmissionType type) {
    final List<TemporaryExposureKey> keys = createValidTemporaryExposureKeys(1);
    final HttpHeaders headers = headers();
    headers.add("cwa-authorization", "foo");

    final SubmissionPayload payload = buildSubmissionPayload(List.of("DE"), "DE", true, keys, type);

    final ResponseEntity<Void> response = rest.postForEntity("/version/v1" + SUBMISSION_ROUTE,
        new HttpEntity<>(payload, headers), Void.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testDiagnosisKeyRollingPeriodIsZeroShouldNotBeSaved() {
    // given
    final List<TemporaryExposureKey> invalidKeys = createValidTemporaryExposureKeys(0);
    final List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);

    final var invalidPayload = buildSubmissionPayload(List.of("DE"), "DE", true, invalidKeys, SUBMISSION_TYPE_PCR_TEST);
    final String tan = "tan";
    final HttpHeaders headers = headers();
    headers.add("cwa-authorization", tan);

    final var validPayload = buildSubmissionPayload(List.of("DE"), "DE", true, validKeys, SUBMISSION_TYPE_PCR_TEST);

    rest.postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(invalidPayload, headers), Void.class);
    assertEquals(0, diagnosisKeyRepository.count());

    rest.postForEntity("/version/v1" + SUBMISSION_ROUTE, new HttpEntity<>(validPayload, headers), Void.class);

    final int expectedNumberOfKeys = validPayload.getKeysList().size() * config.getRandomKeyPaddingMultiplier();
    assertEquals(expectedNumberOfKeys, diagnosisKeyRepository.count());
  }

  @ParameterizedTest
  @ValueSource(strings = { PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB })
  void testKeyInsertionWithMobileClientProtoBuf(final String testFile) throws IOException {
    final List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();
    final SubmissionPayload submissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true, temporaryExposureKeys,
        SUBMISSION_TYPE_PCR_TEST);

    writeSubmissionPayloadProtobufFile(submissionPayload);

    final Path path = Paths.get(testFile);
    final InputStream input = new FileInputStream(path.toFile());
    final SubmissionPayload payload = SubmissionPayload.parseFrom(input);

    logger.info("Submitting payload: " + System.lineSeparator()
        + JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(payload));

    executor.executePost(payload);

    final String presenceVerificationSql = generateDebugSqlStatement(payload);
    logger.info("SQL debugging statement: " + System.lineSeparator() + presenceVerificationSql);
    final Integer result = jdbcTemplate.queryForObject(presenceVerificationSql, Integer.class);

    assertEquals(payload.getKeysList().size(), result);
    assertElementsCorrespondToEachOther(payload, diagnosisKeyService.getDiagnosisKeys(), config);
  }

  /**
   * Test Self-Report Submission (SRS).
   */
  @ParameterizedTest
  @EnumSource(value = SubmissionType.class, names = { "SUBMISSION_TYPE_SRS_.*" }, mode = Mode.MATCH_ANY)
  void testSrsOtpAuth(final SubmissionType type) {
    final List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);
    final HttpHeaders headers = headers();
    headers.add("cwa-otp", "bar");

    final SubmissionPayload validSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true, validKeys, type);

    final int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM self_report_submissions", Integer.class);

    final ResponseEntity<Void> response = rest.postForEntity("/version/v1" + SUBMISSION_ROUTE,
        new HttpEntity<>(validSubmissionPayload, headers), Void.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    final int result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM self_report_submissions", Integer.class);
    assertEquals(before + 1, result);
  }

  @Test
  @SuppressWarnings("deprecation")
  void testSubmissionOnBehalf() {
    final byte[] locationIdHash = new byte[32];
    new Random().nextBytes(locationIdHash);
    final List<CheckInProtectedReport> protectedReports = List.of(
        DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash),
        DataHelpers.buildDefaultEncryptedCheckIn(locationIdHash));
    final List<CheckIn> checkIns = List.of(DataHelpers.buildDefaultCheckIn(), DataHelpers.buildDefaultCheckIn());
    final SubmissionPayload validSubmissionPayload = SubmissionPayload.newBuilder().addAllKeys(Collections.emptyList())
        .addAllVisitedCountries(Collections.emptyList()).setConsentToFederation(false)
        .setSubmissionType(SUBMISSION_TYPE_HOST_WARNING).addAllCheckInProtectedReports(protectedReports)
        .addAllCheckIns(checkIns).build();
    final HttpHeaders headers = headers();
    headers.add("cwa-authorization", "is mocked and returns true :-)");

    final int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM check_in_protected_reports", Integer.class);
    final int none = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM diagnosis_key", Integer.class);

    final ResponseEntity<Void> response = rest.postForEntity("/version/v1" + SUBMISSION_ON_BEHALF_ROUTE,
        new HttpEntity<>(validSubmissionPayload, headers), Void.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    final int result = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM check_in_protected_reports", Integer.class);
    assertEquals(before + 2, result);
    assertEquals(none, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM diagnosis_key", Integer.class));
  }

  private String toBase64(final TemporaryExposureKey key) {
    return BaseEncoding.base64().encode(key.getKeyData().toByteArray());
  }

  @Deprecated
  @Test
  void unencryptedCheckInsEnabledShouldResultInSavingCorrectNumberOfCheckIns() {
    // GIVEN:
    final List<String> visitedCountries = Collections.singletonList("DE");
    final String originCountry = "DE";

    final List<TemporaryExposureKey> temporaryExposureKeys = createValidTemporaryExposureKeys();
    final List<CheckInProtectedReport> protectedReports = Collections.singletonList(buildDefaultEncryptedCheckIn());
    final List<CheckIn> checkins = Collections.singletonList(
        buildDefaultCheckIn());
    final SubmissionPayload submissionPayload = buildSubmissionPayloadWithCheckins(visitedCountries, originCountry,
        true, temporaryExposureKeys, SUBMISSION_TYPE_PCR_TEST, protectedReports, checkins);

    // WHEN:
    final ResponseEntity<Void> result = executor.executePost(submissionPayload);

    // THEN:
    // For the one valid unencrypted checkin we generate one fake checkins and we also save the encrypted checkins
    // which is 1+(1 Fake)+1 = 3 Saved checkins
    assertThat(result.getHeaders().get("cwa-filtered-checkins").get(0)).isEqualTo("0");
    assertThat(result.getHeaders().get("cwa-saved-checkins").get(0)).isEqualTo("3");
  }

  private void writeSubmissionPayloadProtobufFile(final SubmissionPayload submissionPayload) throws IOException {
    Files.createDirectories(Paths.get(PATH_MOBILE_CLIENT_PAYLOAD_PB));
    final File file = new File(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB);
    file.createNewFile();
    submissionPayload
        .writeTo(new FileOutputStream(PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB));
  }
}
