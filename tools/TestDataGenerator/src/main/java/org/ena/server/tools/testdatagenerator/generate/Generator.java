package org.ena.server.tools.testdatagenerator.generate;

import com.google.protobuf.ByteString;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
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
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKey;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucket;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucket.AggregationInterval;
import org.ena.server.common.protocols.generated.RiskScore.RiskLevel;
import org.ena.server.common.protocols.generated.Security.SignedPayload;
import org.ena.server.tools.testdatagenerator.common.Common;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Generator {

  static String shardKey = "DE";
  static String schemaVersion = "v01";

  static void generate(int hours, int exposuresPerHour, boolean forceEmpty, File outputDir,
      File privateKeyFile, File certificateFile)
      throws IOException, CertificateException {

    int numHours = hours % 24;
    if (forceEmpty && numHours == 0) {
      throw new InvalidParameterException("When '--force_empty' is set, then '--hours' mod 24"
          + "must be greater or equal to 1 (otherwise no hourly file would be generated"
          + "that could be empty).");
    }
    FileUtils.deleteDirectory(outputDir);
    outputDir.mkdirs();

    PrivateKey privateKey = Common.getPrivateKeyFromFile(privateKeyFile);
    Certificate certificate = Common.getCertificateFromFile(certificateFile);

    int numDays = Math.floorDiv(hours, 24);
    LocalDateTime startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay();
    LocalDateTime startOfDays = startOfToday.minusDays(numDays);

    File dailyOutput = new File(outputDir.getPath() + "/days");
    dailyOutput.mkdirs();
    writeDailyFiles(startOfDays, numDays, exposuresPerHour * 24, dailyOutput,
        privateKey, certificate);

    File hourlyOutput = new File(outputDir.getPath() + "/hours");
    hourlyOutput.mkdirs();
    writeHourlyFiles(startOfToday, numHours, exposuresPerHour, forceEmpty, hourlyOutput,
        privateKey, certificate);

    System.out.println("DONE");
  }

  static void writeDailyFiles(LocalDateTime startTimestamp, int numDays, int averageExposures,
      File outputDir, PrivateKey privateKey, Certificate certificate) {
    IntegerDistribution distribution = new PoissonDistribution(averageExposures);
    IntStream.range(0, numDays).forEach(currentDay -> {
      LocalDateTime currentDayStartTimestamp = startTimestamp.plusDays(currentDay);
      String outputFileName = Common.getOutputFileName(
          shardKey,
          schemaVersion,
          AggregationInterval.DAILY,
          currentDayStartTimestamp.toEpochSecond(ZoneOffset.UTC)
      );
      File outputFile = new File(outputDir.getPath() + "/" + outputFileName);
      int numExposures = distribution.sample();
      Stream.of(generateTemporaryExposureKeyBucket(
          shardKey,
          currentDayStartTimestamp,
          AggregationInterval.DAILY,
          numExposures
      )).map(TemporaryExposureKeyBucket::toByteArray)
          .map(Common.uncheckedFunction(
              bytes -> generateSignedPayload(bytes, privateKey, certificate)))
          .map(SignedPayload::toByteArray)
          .forEach(Common.uncheckedConsumer(
              bytes -> writeBytesToFile(bytes, outputFile)));
    });
  }

  static void writeHourlyFiles(LocalDateTime startTimestamp, int numHours, int averageExposures,
      boolean forceEmpty, File outputDir, PrivateKey privateKey, Certificate certificate) {
    IntegerDistribution distribution = new PoissonDistribution(averageExposures);
    IntStream.range(0, numHours).forEach(currentHour -> {
      LocalDateTime currentHourStartTimestamp = startTimestamp.plusHours(currentHour);
      String outputFileName = Common.getOutputFileName(
          shardKey,
          schemaVersion,
          AggregationInterval.HOURLY,
          currentHourStartTimestamp.toEpochSecond(ZoneOffset.UTC)
      );
      File outputFile = new File(outputDir.getPath() + "/" + outputFileName);
      int numExposures;
      if (currentHour == 0 && forceEmpty) {
        numExposures = 0;
      } else {
        numExposures = distribution.sample();
      }
      Stream.of(generateTemporaryExposureKeyBucket(
          shardKey,
          currentHourStartTimestamp,
          AggregationInterval.HOURLY,
          numExposures
      )).map(TemporaryExposureKeyBucket::toByteArray)
          .map(Common.uncheckedFunction(
              bytes -> generateSignedPayload(bytes, privateKey, certificate)))
          .map(SignedPayload::toByteArray)
          .forEach(Common.uncheckedConsumer(
              bytes -> writeBytesToFile(bytes, outputFile)));
    });
  }

  static TemporaryExposureKeyBucket generateTemporaryExposureKeyBucket(
      String shardKey,
      LocalDateTime timestamp,
      AggregationInterval aggregationInterval,
      int numExposureKeys
  ) {
    long timestampEpochSeconds = timestamp.toEpochSecond(ZoneOffset.UTC);
    int aggregationTimeHours = aggregationInterval == AggregationInterval.HOURLY ? 1 : 24;
    long aggregationTimeSeconds = TimeUnit.HOURS.toSeconds(aggregationTimeHours);
    List<TemporaryExposureKey> temporaryExposureKeys = IntStream.range(0, numExposureKeys)
        .mapToObj(i -> {
          long rollingStartEpochSeconds = Common.getRandomBetween(
              timestampEpochSeconds,
              timestampEpochSeconds + aggregationTimeSeconds
          );
          //Convert from epoch seconds to 10 minute increment counter
          int rollingStartNumber = Math.toIntExact(Math.floorDiv(
              rollingStartEpochSeconds,
              TimeUnit.MINUTES.toSeconds(10)
          ));
          return generateTemporaryExposureKey(rollingStartNumber);
        }).collect(Collectors.toList());
    return TemporaryExposureKeyBucket.newBuilder()
        .setShardKey(shardKey)
        .setTimestamp(Math.toIntExact(timestampEpochSeconds))
        .setAggregationInterval(aggregationInterval)
        .addAllExposureKeys(temporaryExposureKeys)
        .build();
  }

  static TemporaryExposureKey generateTemporaryExposureKey(int rollingStartNumber) {
    RiskLevel riskLevel = RiskLevel.forNumber(
        Common.getRandomBetween(
            RiskLevel.LOWEST_VALUE,
            RiskLevel.HIGHEST_VALUE
        )
    );
    return TemporaryExposureKey.newBuilder()
        .setKeyData(ByteString.copyFrom(generateDiagnosisKeyBytes()))
        .setRollingStartNumber(rollingStartNumber)
        .setRiskLevel(riskLevel)
        .build();
  }

  static byte[] generateDiagnosisKeyBytes() {
    byte[] exposureKey = new byte[16];
    new Random().nextBytes(exposureKey);
    return exposureKey;
  }

  static SignedPayload generateSignedPayload(byte[] payload, PrivateKey privateKey,
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

  static void writeBytesToFile(byte[] bytes, File outputFile) throws IOException {
    outputFile.createNewFile();
    FileOutputStream outputFileStream = new FileOutputStream(outputFile);
    outputFileStream.write(bytes);
  }
}
