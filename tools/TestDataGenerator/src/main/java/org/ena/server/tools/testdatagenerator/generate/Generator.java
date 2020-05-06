package org.ena.server.tools.testdatagenerator.generate;

import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKey;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucket;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucket.AggregationInterval;
import org.ena.server.common.protocols.generated.RiskScore.RiskLevel;
import org.ena.server.common.protocols.generated.RiskScore.RiskScoreParameters;
import org.ena.server.common.protocols.generated.RiskScore.RiskScoreParameters.AttenuationRiskParameter;
import org.ena.server.common.protocols.generated.RiskScore.RiskScoreParameters.DaysSinceLastExposureRiskParameter;
import org.ena.server.common.protocols.generated.RiskScore.RiskScoreParameters.DurationRiskParameter;
import org.ena.server.common.protocols.generated.RiskScore.RiskScoreParameters.TransmissionRiskParameter;
import org.ena.server.common.protocols.generated.Security.SignedPayload;
import org.ena.server.tools.testdatagenerator.common.Common;
import org.ena.server.tools.testdatagenerator.common.Common.DirectoryIndex;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Generator {

  static DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  static String country = "DE";
  static String version = "v1";

  static void generate(int totalHours, String startDateStr, int exposuresPerHour, File openapi,
                       File outputDirectory, File privateKeyFile, File certificateFile, int seed)
      throws IOException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {

    File rootDirectory = createRootDirectoryStructure(outputDirectory);
    if (openapi != null && openapi.exists()) {
      File target = Common.makeFile(rootDirectory, "index");
      Files.copy(openapi.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    File dateDirectory = createDiagnosisKeyDirectoryStructure(rootDirectory);

    LocalDate startDate = LocalDate.parse(startDateStr, ISO8601);
    DirectoryIndex<LocalDate> dateDirectoryIndex =
        createDateDirectoryIndex(dateDirectory, startDate, totalHours);

    createHourDirectoryIndex(dateDirectoryIndex, startDate, totalHours);

    Random random = new Random(seed);
    List<List<TemporaryExposureKeyBucket>> hourData =
        generateHourData(startDate, totalHours, exposuresPerHour, random);

    List<TemporaryExposureKeyBucket> dayData = aggregateDayData(startDate, hourData);

    PrivateKey privateKey = Common.getPrivateKeyFromFile(privateKeyFile);
    Certificate certificate = Common.getCertificateFromFile(certificateFile);
    List<List<SignedPayload>> signedHourData = hourData.stream()
        .map(dayList -> dayList.stream()
            .map(TemporaryExposureKeyBucket::toByteArray)
            .map(Common.uncheckedFunction(hourBytes -> generateSignedPayload(
                hourBytes,
                privateKey,
                certificate)))
            .collect(Collectors.toList()))
        .collect(Collectors.toList());

    List<SignedPayload> signedDayData = dayData.stream()
        .map(TemporaryExposureKeyBucket::toByteArray)
        .map(Common.uncheckedFunction(hourBytes -> generateSignedPayload(
            hourBytes,
            privateKey,
            certificate)))
        .collect(Collectors.toList());

    // Write signed hour data
    IntStream.range(0, signedHourData.size())
        .forEach(dayIndex -> Stream.of(dayIndex)
            .map(__ -> outputDirectory.toPath()
                .resolve("version")
                .resolve("v1")
                .resolve("diagnosis-keys")
                .resolve("country")
                .resolve("DE")
                .resolve("date")
                .resolve(ISO8601.format(startDate.plusDays(dayIndex)))
                .resolve("hour")
                .toFile())
            .forEach(hourDirectory -> IntStream.range(0, getHours(
                startDate, startDate.plusDays(dayIndex), totalHours
            ).size())
                .forEach(hourIndex -> Stream.of(hourIndex)
                    .map(__ -> Common.makeDirectory(hourDirectory, String.valueOf(hourIndex)))
                    .map(a -> Common.makeFile(a, "index"))
                    .forEach(indexFile -> Common.writeBytesToFile(
                        signedHourData.get(dayIndex).get(hourIndex).toByteArray(),
                        indexFile)))));

    // Write signed day data
    IntStream.range(0, signedDayData.size())
        .forEach(dayIndex -> Stream.of(dayIndex)
            .map(__ -> outputDirectory.toPath()
                .resolve("version")
                .resolve("v1")
                .resolve("diagnosis-keys")
                .resolve("country")
                .resolve("DE")
                .resolve("date")
                .resolve(ISO8601.format(startDate.plusDays(dayIndex)))
                .toFile()
            )
            .map(dayDirectory -> Common.makeFile(dayDirectory, "index"))
            .forEach(indexFile -> Common.writeBytesToFile(
                signedDayData.get(dayIndex).toByteArray(),
                indexFile)));

    // Write parameters
    File parametersDirectory = createParametersDirectoryStructure(rootDirectory);
    File parametersFile = Common.makeFile(parametersDirectory, "index");
    RiskScoreParameters riskScoreParameters = RiskScoreParameters.newBuilder()
        .setAttenuation(AttenuationRiskParameter.newBuilder()
            .setGt73Dbm(RiskLevel.LOWEST)
            .setGt63Le73Dbm(RiskLevel.LOW)
            .setGt51Le63Dbm(RiskLevel.LOW_MEDIUM)
            .setGt33Le51Dbm(RiskLevel.MEDIUM)
            .setGt27Le33Dbm(RiskLevel.MEDIUM_HIGH)
            .setGt10Le15Dbm(RiskLevel.HIGH)
            .setGt10Le15Dbm(RiskLevel.VERY_HIGH)
            .setLt10Dbm(RiskLevel.HIGHEST)
            .build())
        .setDaysSinceLastExposure(DaysSinceLastExposureRiskParameter.newBuilder()
            .setGe14Days(RiskLevel.LOWEST)
            .setGe12Lt14Days(RiskLevel.LOW)
            .setGe10Lt12Days(RiskLevel.LOW_MEDIUM)
            .setGe8Lt10Days(RiskLevel.MEDIUM)
            .setGe6Lt8Days(RiskLevel.MEDIUM_HIGH)
            .setGe4Lt6Days(RiskLevel.HIGH)
            .setGe2Lt4Days(RiskLevel.VERY_HIGH)
            .setGe0Lt2Days(RiskLevel.HIGHEST)
            .build())
        .setDuration(DurationRiskParameter.newBuilder()
            .setEq0Min(RiskLevel.LOWEST)
            .setGt0Le5Min(RiskLevel.LOW)
            .setGt5Le10Min(RiskLevel.LOW_MEDIUM)
            .setGt10Le15Min(RiskLevel.MEDIUM)
            .setGt15Le20Min(RiskLevel.MEDIUM_HIGH)
            .setGt20Le25Min(RiskLevel.HIGH)
            .setGt25Le30Min(RiskLevel.VERY_HIGH)
            .setGt30Min(RiskLevel.HIGHEST)
            .build())
        .setTransmission(TransmissionRiskParameter.newBuilder()
            .setAppDefined1(RiskLevel.LOWEST)
            .setAppDefined2(RiskLevel.LOW)
            .setAppDefined3(RiskLevel.LOW_MEDIUM)
            .setAppDefined4(RiskLevel.MEDIUM)
            .setAppDefined5(RiskLevel.MEDIUM_HIGH)
            .setAppDefined6(RiskLevel.HIGH)
            .setAppDefined7(RiskLevel.VERY_HIGH)
            .setAppDefined8(RiskLevel.HIGHEST)
            .build())
        .build();
    SignedPayload signedRiskScoreParameters = generateSignedPayload(
        riskScoreParameters.toByteArray(),
        privateKey,
        certificate);
    Common.writeBytesToFile(signedRiskScoreParameters.toByteArray(), parametersFile);

    System.out.println("DONE");
  }

  static File createRootDirectoryStructure(File outputDirectory) {
    return Stream.of(outputDirectory)
        .peek(Common.uncheckedConsumer(FileUtils::deleteDirectory))
        .peek(File::mkdirs)
        .map(directory -> Common.makeDirectory(directory, "version"))
        .peek(directory -> Common.writeIndex(directory, Collections.singletonList(version)))
        .map(directory -> Common.makeDirectory(directory, version))
        .findFirst()
        .orElseThrow();
  }

  static File createDiagnosisKeyDirectoryStructure(File outputDirectory) {
    // API: /version/{version}/diagnosis-keys/country/{country}/date/{date}/hour/{hour}
    return Stream.of(outputDirectory)
        .map(directory -> Common.makeDirectory(directory, "diagnosis-keys"))
        .map(directory -> Common.makeDirectory(directory, "country"))
        .peek(directory -> Common.writeIndex(directory, Collections.singletonList(country)))
        .map(directory -> Common.makeDirectory(directory, country))
        .map(directory -> Common.makeDirectory(directory, "date"))
        .findFirst()
        .orElseThrow();
  }

  static File createParametersDirectoryStructure(File outputDirectory) {
    // API: /version/{version}/parameters/country/{country}
    return Stream.of(outputDirectory)
        .map(directory -> Common.makeDirectory(directory, "parameters"))
        .map(directory -> Common.makeDirectory(directory, "country"))
        .peek(directory -> Common.writeIndex(directory, Collections.singletonList(country)))
        .map(directory -> Common.makeDirectory(directory, country))
        .findFirst()
        .orElseThrow();
  }

  static DirectoryIndex<LocalDate> createDateDirectoryIndex(File dateDirectory,
                                                            LocalDate startDate, int totalHours) {
    int numDays = getNumberOfDays(totalHours);
    return Stream.of(dateDirectory)
        .map(directory -> new DirectoryIndex<>(directory, getDates(startDate, numDays)))
        .peek(index -> Common.writeIndex(index, date -> ISO8601.format((TemporalAccessor) date)))
        .findFirst()
        .orElseThrow();
  }

  static List<DirectoryIndex<LocalDateTime>> createHourDirectoryIndex(
      DirectoryIndex<LocalDate> directoryIndex, LocalDate startDate, int totalHours) {
    return Stream.of(directoryIndex)
        .flatMap(dates -> dates.index.stream()
            .flatMap(currentDate -> Stream.of(currentDate)
                .map(__ -> Common.makeDirectory(dates.directory, ISO8601.format(currentDate)))
                .map(directory -> Common.makeDirectory(directory, "hour"))
                .map(directory -> new DirectoryIndex<>(
                    directory,
                    getHours(startDate, currentDate, totalHours)))))
        .peek(index -> Common.writeIndex(index, hour -> ((LocalDateTime) hour).getHour()))
        .collect(Collectors.toList());
  }

  static List<List<TemporaryExposureKeyBucket>> generateHourData(LocalDate startDate,
                                                                 int totalHours, int exposuresPerHour, Random random) {
    int numDays = getNumberOfDays(totalHours);
    return getDates(startDate, numDays).stream()
        .map(currentDate -> getHours(startDate, currentDate, totalHours).stream()
            .map(currentHour -> generateTemporaryExposureKeyBucket(
                country,
                currentHour,
                AggregationInterval.HOURLY,
                Common.nextPoisson(exposuresPerHour, random),
                random))
            .collect(Collectors.toList()))
        .collect(Collectors.toList());
  }

  static List<TemporaryExposureKeyBucket> aggregateDayData(LocalDate startDate,
                                                           List<List<TemporaryExposureKeyBucket>> hourData) {
    int numDays = hourData.size();
    // Last (incomplete) day does not get an aggregate
    return IntStream.range(0, numDays - 1)
        .mapToObj(currentDateIndex -> Stream.of(currentDateIndex)
            .map(startDate::plusDays)
            .map(currentDate -> TemporaryExposureKeyBucket.newBuilder()
                .setShardKey(country)
                .setTimestamp(Math.toIntExact(startDate.toEpochSecond(startDate.atStartOfDay().toLocalTime(), ZoneOffset.UTC)))
                .setAggregationInterval(AggregationInterval.DAILY)
                .addAllExposureKeys(hourData.get(currentDateIndex).stream()
                    .flatMap(hour -> hour.getExposureKeysList().stream())
                    .collect(Collectors.toList())
                )
                .build()
            )
            .collect(Collectors.toList())
        )
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  static int getNumberOfDays(int hours) {
    return -Math.floorDiv(-hours, 24);
  }

  static List<LocalDate> getDates(LocalDate startDate, int numDays) {
    return IntStream.range(0, numDays)
        .mapToObj(startDate::plusDays)
        .collect(Collectors.toList());
  }

  static List<LocalDateTime> getHours(LocalDate startDate, LocalDate currentDate, int totalHours) {
    int numFullDays = Math.floorDiv(totalHours, 24);
    long currentDay = ChronoUnit.DAYS.between(startDate, currentDate);
    int lastHour = (currentDay < numFullDays) ? 24 : totalHours % 24;

    return IntStream.range(0, lastHour)
        .mapToObj(hour -> currentDate.atStartOfDay().plusHours(hour))
        .collect(Collectors.toList());
  }

  static TemporaryExposureKeyBucket generateTemporaryExposureKeyBucket(
      String shardKey,
      LocalDateTime timestamp,
      AggregationInterval aggregationInterval,
      int numExposureKeys,
      Random random
  ) {
    long timestampEpochSeconds = timestamp.toEpochSecond(ZoneOffset.UTC);
    long aggregationTimeHours = aggregationInterval == AggregationInterval.HOURLY ? 1L : 24L;
    long aggregationTimeSeconds = TimeUnit.HOURS.toSeconds(aggregationTimeHours);
    List<TemporaryExposureKey> temporaryExposureKeys = IntStream.range(0, numExposureKeys)
        .mapToObj(i -> {
          long rollingStartEpochSeconds = Common.getRandomBetween(
              timestampEpochSeconds,
              timestampEpochSeconds + aggregationTimeSeconds,
              random);
          //Convert from epoch seconds to 10 minute increment counter
          long rollingStartNumber = Math.floorDiv(
              rollingStartEpochSeconds,
              TimeUnit.MINUTES.toSeconds(10));
          return generateTemporaryExposureKey(rollingStartNumber, random);
        }).collect(Collectors.toList());
    return TemporaryExposureKeyBucket.newBuilder()
        .setShardKey(shardKey)
        .setTimestamp(timestampEpochSeconds)
        .setAggregationInterval(aggregationInterval)
        .addAllExposureKeys(temporaryExposureKeys)
        .build();
  }

  static TemporaryExposureKey generateTemporaryExposureKey(long rollingStartNumber, Random random) {
    RiskLevel riskLevel = RiskLevel.forNumber(
        Common.getRandomBetween(RiskLevel.LOWEST_VALUE, RiskLevel.HIGHEST_VALUE, random));
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFrom(generateDiagnosisKeyBytes(random)))
        .setRollingStartNumber(rollingStartNumber)
        .setRiskLevel(riskLevel)
        .build();
  }

  static byte[] generateDiagnosisKeyBytes(Random random) {
    byte[] exposureKey = new byte[16];
    random.nextBytes(exposureKey);
    return exposureKey;
  }

  static <T> SignedPayload generateSignedPayload(byte[] payload, PrivateKey privateKey,
                                                 Certificate certificate)
      throws CertificateEncodingException, InvalidKeyException, NoSuchProviderException, NoSuchAlgorithmException, SignatureException {
    Signature payloadSignature = Signature.getInstance("Ed25519", "BC");
    payloadSignature.initSign(privateKey);
    payloadSignature.update(payload);
    return SignedPayload.newBuilder()
        .setCertificateChain(ByteString.copyFrom(certificate.getEncoded()))
        .setSignature(ByteString.copyFrom(payloadSignature.sign()))
        .setPayload(ByteString.copyFrom(payload))
        .build();
  }
}
