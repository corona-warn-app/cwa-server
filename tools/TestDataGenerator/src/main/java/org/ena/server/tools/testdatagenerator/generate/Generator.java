package org.ena.server.tools.testdatagenerator.generate;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import java.io.File;
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
import java.text.ParseException;
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
import javax.print.attribute.standard.MediaSize.ISO;
import org.apache.commons.io.FileUtils;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKey;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucket;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucket.AggregationInterval;
import org.ena.server.common.protocols.generated.ExposureKeys.TemporaryExposureKeyBucketOrBuilder;
import org.ena.server.common.protocols.generated.RiskScore.RiskLevel;
import org.ena.server.common.protocols.generated.Security.SignedPayload;
import org.ena.server.tools.testdatagenerator.common.Common;
import org.ena.server.tools.testdatagenerator.common.Common.DirectoryIndex;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Generator {

  static DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  static String country = "DE";
  static String version = "v1";

  static void generate(int totalHours, String startDateStr, int exposuresPerHour,
      boolean forceEmpty,
      File outputDirectory,
      File privateKeyFile, File certificateFile, int seed)
      throws IOException, CertificateException, ParseException {

    int numLeftoverHours = totalHours % 24;
    if (forceEmpty && numLeftoverHours == 0) {
      throw new InvalidParameterException("When '--force_empty' is set, then '--hours' mod 24"
          + "must be greater or equal to 1 (otherwise no hourly file would be generated"
          + "that could be empty).");
    }

    File dateDirectory = createDirectoryStructure(outputDirectory);

    LocalDate startDate = LocalDate.parse(startDateStr, ISO8601);
    DirectoryIndex<LocalDate> dateDirectoryIndex = createDateDirectoryIndex(
        dateDirectory,
        startDate,
        totalHours
    );

    createHourDirectoryIndex(
        dateDirectoryIndex,
        startDate,
        totalHours
    );

    Random random = new Random(seed);
    List<List<TemporaryExposureKeyBucket>> hourData = generateHourData(
        startDate,
        totalHours,
        exposuresPerHour,
        random
    );

    List<TemporaryExposureKeyBucket> dayData = aggregateDayData(
        startDate,
        hourData
    );

    PrivateKey privateKey = Common.getPrivateKeyFromFile(privateKeyFile);
    Certificate certificate = Common.getCertificateFromFile(certificateFile);
    List<List<SignedPayload>> signedHourData = hourData.stream()
        .map(dayList -> dayList.stream()
            .map(TemporaryExposureKeyBucket::toByteArray)
            .map(Common.uncheckedFunction(hourBytes -> generateSignedPayload(
                hourBytes,
                privateKey,
                certificate
            )))
            .collect(Collectors.toList())
        )
        .collect(Collectors.toList());

    List<SignedPayload> signedDayData = dayData.stream()
        .map(a -> a)
        .map(TemporaryExposureKeyBucket::toByteArray)
        .map(Common.uncheckedFunction(hourBytes -> generateSignedPayload(
            hourBytes,
            privateKey,
            certificate
        )))
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
                .toFile()
            )
            .forEach(hourDirectory -> IntStream.range(0, getHours(
                    startDate, startDate.plusDays(dayIndex), totalHours
                ).size())
                .forEach(hourIndex -> Stream.of(hourIndex)
                    .map(__ -> Common.makeDirectory(hourDirectory, String.valueOf(hourIndex)))
                    .map(a -> Common.makeFile(a, "index"))
                    .forEach(indexFile -> Common.writeBytesToFile(
                        signedHourData.get(dayIndex).get(hourIndex).toByteArray(),
                        indexFile
                    ))
                )
            )
        );

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
                indexFile
            ))
        );

    System.out.println("DONE");
  }

  static File createDirectoryStructure(File outputDirectory) {
    // API: /version/{version}/diagnosis-keys/country/{country}/date/{date}/hour/{hour}
    return Stream.of(outputDirectory)
        .peek(Common.uncheckedConsumer(FileUtils::deleteDirectory))
        .peek(File::mkdirs)
        .map(directory -> Common.makeDirectory(directory, "version"))
        .peek(directory -> Common.writeIndex(directory, Collections.singletonList(version)))
        .map(directory -> Common.makeDirectory(directory, version))
        .map(directory -> Common.makeDirectory(directory, "diagnosis-keys"))
        .map(directory -> Common.makeDirectory(directory, "country"))
        .peek(directory -> Common.writeIndex(directory, Collections.singletonList(country)))
        .map(directory -> Common.makeDirectory(directory, country))
        .map(directory -> Common.makeDirectory(directory, "date"))
        .findFirst()
        .orElseThrow();
  }

  static DirectoryIndex<LocalDate> createDateDirectoryIndex(File dateDirectory,
      LocalDate startDate, int totalHours) {
    int numDays = getNumberOfDays(totalHours);
    return Stream.of(dateDirectory)
        .map(directory -> new DirectoryIndex<>(
            directory,
            getDates(startDate, numDays))
        )
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
                    getHours(startDate, currentDate, totalHours)
                ))
            )
        )
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
                random
            ))
            .collect(Collectors.toList())
        )
        .collect(Collectors.toList());
  }

  static List<TemporaryExposureKeyBucket> aggregateDayData(LocalDate startDate,
      List<List<TemporaryExposureKeyBucket>> hourData) {
    int numDays = hourData.size();
    return IntStream.range(0, numDays)
        .mapToObj(currentDateIndex -> Stream.of(currentDateIndex)
            .map(startDate::plusDays)
            .map(currentDate -> TemporaryExposureKeyBucket.newBuilder()
                .setShardKey(country)
                .setTimestamp(Math.toIntExact(
                    startDate.toEpochSecond(startDate.atStartOfDay().toLocalTime(), ZoneOffset.UTC))
                )
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

  static int getNumberOfDays (int hours) {
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
    int lastHour;
    if (currentDay < numFullDays) {
      lastHour = 24;
    } else {
      lastHour = totalHours % 24;
    }
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
    int aggregationTimeHours = aggregationInterval == AggregationInterval.HOURLY ? 1 : 24;
    long aggregationTimeSeconds = TimeUnit.HOURS.toSeconds(aggregationTimeHours);
    List<TemporaryExposureKey> temporaryExposureKeys = IntStream.range(0, numExposureKeys)
        .mapToObj(i -> {
          long rollingStartEpochSeconds = Common.getRandomBetween(
              timestampEpochSeconds,
              timestampEpochSeconds + aggregationTimeSeconds,
              random
          );
          //Convert from epoch seconds to 10 minute increment counter
          int rollingStartNumber = Math.toIntExact(Math.floorDiv(
              rollingStartEpochSeconds,
              TimeUnit.MINUTES.toSeconds(10)
          ));
          return generateTemporaryExposureKey(rollingStartNumber, random);
        }).collect(Collectors.toList());
    return TemporaryExposureKeyBucket.newBuilder()
        .setShardKey(shardKey)
        .setTimestamp(Math.toIntExact(timestampEpochSeconds))
        .setAggregationInterval(aggregationInterval)
        .addAllExposureKeys(temporaryExposureKeys)
        .build();
  }

  static TemporaryExposureKey generateTemporaryExposureKey(int rollingStartNumber, Random random) {
    RiskLevel riskLevel = RiskLevel.forNumber(
        Common.getRandomBetween(
            RiskLevel.LOWEST_VALUE,
            RiskLevel.HIGHEST_VALUE,
            random
        )
    );
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
