package app.coronawarn.server.services.submission.integration;

import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildTemporaryExposureKeyWithoutMultiplying;
import static app.coronawarn.server.services.submission.SubmissionPayloadGenerator.buildTemporaryExposureKeys;
import static app.coronawarn.server.services.submission.assertions.SubmissionAssertions.assertElementsCorrespondToEachOther;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import app.coronawarn.server.common.persistence.repository.DiagnosisKeyRepository;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.common.protocols.external.exposurenotification.ReportType;
import app.coronawarn.server.common.protocols.external.exposurenotification.TemporaryExposureKey;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.Builder;
import app.coronawarn.server.common.protocols.internal.SubmissionPayload.SubmissionType;
import app.coronawarn.server.services.submission.config.SubmissionServiceConfig;
import app.coronawarn.server.services.submission.controller.FakeDelayManager;
import app.coronawarn.server.services.submission.controller.RequestExecutor;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("integration-test")
@Sql(scripts = {"classpath:db/clean_db_state.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
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
  private FakeDelayManager fakeDelayManager;

  @BeforeEach
  public void setUpMocks() {
    when(tanVerifier.verifyTan(anyString())).thenReturn(true);
    when(fakeDelayManager.getJitteredFakeDelay()).thenReturn(1000L);
  }

  @Test
  void testDiagnosisKeyRollingPeriodIsZeroShouldNotBeSaved() {

    // given
    List<TemporaryExposureKey> invalidKeys = createValidTemporaryExposureKeys(0);

    List<TemporaryExposureKey> validKeys = createValidTemporaryExposureKeys(1);

    SubmissionPayload invalidSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        invalidKeys, SubmissionType.SUBMISSION_TYPE_PCR_TEST);
    String tan = "tan";
    HttpHeaders headers = new HttpHeaders();
    headers.add("cwa-fake", "0");
    headers.add("cwa-authorization", tan);
    headers.setContentType(MediaType.valueOf("Application/x-protobuf"));

    SubmissionPayload validSubmissionPayload = buildSubmissionPayload(List.of("DE"), "DE", true,
        validKeys, SubmissionType.SUBMISSION_TYPE_PCR_TEST);

    testRestTemplate
        .postForEntity("/version/v1/diagnosis-keys", new HttpEntity<>(invalidSubmissionPayload, headers), Void.class);
    assertEquals(0, diagnosisKeyRepository.count());

    testRestTemplate
        .postForEntity("/version/v1/diagnosis-keys", new HttpEntity<>(validSubmissionPayload, headers), Void.class);

    final int expectedNumberOfKeys =
        validSubmissionPayload.getKeysList().size() * config.getRandomKeyPaddingMultiplier();
    assertEquals(expectedNumberOfKeys, diagnosisKeyRepository.count());
  }


  @ParameterizedTest
  @ValueSource(strings = {PATH_MOBILE_CLIENT_PAYLOAD_PB + "/" + FILENAME_MOBILE_CLIENT_PAYLOAD_PB})
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

  private SubmissionPayload buildSubmissionPayload(List<String> visitedCountries, String originCountry,
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

  private List<TemporaryExposureKey> createValidTemporaryExposureKeys(int rollingPeriod) {
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

  private List<TemporaryExposureKey> createValidTemporaryExposureKeys() {
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

  private Builder submissionPayloadWithOriginCountry(Builder submissionPayload, String originCountry) {
    return submissionPayload.setOrigin(originCountry);
  }

  private Builder submissionPayloadWithVisitedCountries(Builder submissionPayload, List<String> visitedCountries) {
    return submissionPayload.addAllVisitedCountries(visitedCountries);
  }

  private Builder submissionPayloadWithConsentToFederation(Builder submissionPayload, boolean consentToFederation) {
    return submissionPayload.setConsentToFederation(consentToFederation);
  }

  private Builder submissionPayloadWithSubmissionType(Builder submissionPayload, SubmissionType submissionType) {
    return submissionPayload.setSubmissionType(submissionType);
  }
}
