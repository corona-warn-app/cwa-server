package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory.DiagnosisKeysDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.io.FileUtils;
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

  private static final Logger logger = LoggerFactory
      .getLogger(DiagnosisKeyDistributionRunner.class);
  private static final String VERSION_DIRECTORY = "version";
  private static final String VERSION = "v1";
  private static final String OUTPUT_PATH = "out";

  @Autowired
  private CryptoProvider cryptoProvider;

  @Autowired
  private DiagnosisKeyService diagnosisKeyService;

  @Override
  public void run(ApplicationArguments args) throws IOException {
    Collection<DiagnosisKey> diagnosisKeys = diagnosisKeyService.getDiagnosisKeys();

    DiagnosisKeysDirectoryImpl diagnosisKeysDirectory =
        new DiagnosisKeysDirectoryImpl(diagnosisKeys, cryptoProvider);

    IndexDirectory<?> versionDirectory =
        new IndexDirectoryImpl<>(VERSION_DIRECTORY, __ -> Set.of(VERSION), Object::toString);

    versionDirectory.addDirectoryToAll(__ -> diagnosisKeysDirectory);

    java.io.File outputDirectory = new File(OUTPUT_PATH);
    clearDirectory(outputDirectory);
    DirectoryImpl root = new DirectoryImpl(outputDirectory);
    root.addDirectory(new IndexingDecorator<>(versionDirectory));

    logger.debug("Preparing ...");
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
