package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.DiagnosisKeysDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
/**
 * This runner retrieves stored diagnosis keys, the generates and persists the respective diagnosis
 * key bundles.
 */
public class DiagnosisKeyDistributionRunner implements ApplicationRunner {

  private static final Logger logger =
      LoggerFactory.getLogger(DiagnosisKeyDistributionRunner.class);
  private static final String COUNTRY = "DE";
  private static final String VERSION = "v1";
  private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String OUTPUT_PATH = "out";

  @Autowired
  private CryptoProvider cryptoProvider;

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Override
  public void run(ApplicationArguments args) throws IOException {

    Collection<DiagnosisKey> diagnosisKeys = diagnosisKeyService.getDiagnosisKeys();

    java.io.File outputDirectory = new File(OUTPUT_PATH);

    clearDirectory(outputDirectory);

    RandomGenerator random = new JDKRandomGenerator();
    random.setSeed(123456);

    int totalHours = 330;
    String startDateStr = "2020-05-01";
    int exposuresPerHour = 100;

    LocalDate startDate = LocalDate.parse(startDateStr, ISO8601);

    DiagnosisKeysDirectoryImpl diagnosisKeysDirectory = new DiagnosisKeysDirectoryImpl(
        diagnosisKeys, COUNTRY, ISO8601, cryptoProvider);

    IndexDirectory<?> versionDirectory =
        new IndexDirectoryImpl<>("version", __ -> List.of(VERSION), Object::toString);

    versionDirectory.addDirectoryToAll(__ -> diagnosisKeysDirectory);

    DirectoryImpl root = new DirectoryImpl(outputDirectory);
    root.addDirectory(new IndexingDecorator<>(versionDirectory));
    logger.debug("Generating ...");
    root.prepare(new Stack<>());
    logger.debug("Writing ...");
    root.write();

    logger.info("DONE");
  }

  private static void clearDirectory(File directory) throws IOException {
    FileUtils.deleteDirectory(directory);
    directory.mkdirs();
  }
}
