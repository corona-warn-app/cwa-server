package app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory;

import static java.lang.String.join;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoProvider.class},
    initializers = ConfigFileApplicationContextInitializer.class)
public class DiagnosisKeysDirectory {

  @Autowired
  CryptoProvider cryptoProvider;

  @Rule
  private TemporaryFolder outputFolder = new TemporaryFolder();

  private File outputFile;
  private Directory parentDirectory;

  List<DiagnosisKey> diagnosisKeys;

  @BeforeEach
  void setupAll() throws IOException {
    outputFolder.create();
    outputFile = outputFolder.newFolder();
    parentDirectory = new DirectoryImpl(outputFile);

    // 01.01.1970 - 00:00 UTC
    long startTimestamp = 0;
    // 02.01.1970 - 06:00 UTC (in 10 minute intervals)
    long endTimestamp = startTimestamp + (24 * 6);

    // Generate diagnosis keys covering 30 hours of submission timestamps
    // Until 02.01.1970 - 06:00 UTC -> 1 full day + 6 hours
    diagnosisKeys = IntStream.range(0, 30)
        .mapToObj(currentHour -> generateDiagnosisKey(startTimestamp + currentHour))
        .collect(Collectors.toList());
  }

  private DiagnosisKey generateDiagnosisKey(long submissionTimestamp) {
    return DiagnosisKey.builder()
        .withKeyData(new byte[16])
        .withRollingStartNumber(1L)
        .withRollingPeriod(2L)
        .withTransmissionRiskLevel(3)
        .withSubmissionTimestamp(submissionTimestamp)
        .build();
  }

  @Test
  public void checkBuildsTheCorrectDirectoryStructureWhenNoKeys() {
    diagnosisKeys = new ArrayList<>();
    Directory directory = new DiagnosisKeysDirectoryImpl(diagnosisKeys, cryptoProvider);
    parentDirectory.addDirectory(directory);
    directory.prepare(new Stack<>());
    directory.write();

    String s = File.separator;
    Set<String> expectedFiles = Set.of(
        join(s, "diagnosis-keys", "country", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "0", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "1", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "2", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "3", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "4", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "5", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "6", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "7", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "8", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "9", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "10", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "11", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "12", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "13", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "14", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "15", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "16", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "17", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "18", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "19", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "20", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "21", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "22", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "01-01-1970", "hour", "23", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "hour", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "hour", "0", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "hour", "1", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "hour", "2", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "hour", "3", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "hour", "4", "index"),
        join(s, "diagnosis-keys", "country", "DE", "date", "02-01-1970", "hour", "5", "index")
    );

    Set<String> actualFiles = getActualFiles(outputFile);

    assertEquals(expectedFiles, actualFiles);
  }

  private Set<String> getActualFiles(java.io.File root) {
    Set<String> files = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isFile)
        .map(File::getAbsolutePath)
        .map(path -> path.substring(outputFile.getAbsolutePath().length() - 1))
        .collect(Collectors.toSet());

    Set<java.io.File> directories = Arrays.stream(Objects.requireNonNull(root.listFiles()))
        .filter(File::isDirectory)
        .collect(Collectors.toSet());

    Set<String> subFiles = directories.stream()
        .map(this::getActualFiles)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    files.addAll(subFiles);
    return files;
  }
}
