package app.coronawarn.server.services.distribution.runner;

import app.coronawarn.server.common.persistence.domain.DiagnosisKey;
import app.coronawarn.server.common.persistence.service.DiagnosisKeyService;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory.DiagnosisKeysDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${app.coronawarn.server.services.distribution.version}")
  private String version;

  @Value("${app.coronawarn.server.services.distribution.paths.output}")
  private String outputPath;

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
        new IndexDirectoryImpl<>(VERSION_DIRECTORY, __ -> Set.of(version), Object::toString);

    versionDirectory.addDirectoryToAll(__ -> diagnosisKeysDirectory);

    java.io.File outputDirectory = new File(outputPath);
    clearDirectory(outputDirectory);
    DirectoryImpl root = new DirectoryImpl(outputDirectory);
    root.addDirectory(new IndexingDecorator<>(versionDirectory));

    root.prepare(new ImmutableStack<>());
    root.write();
    logger.debug("Diagnosis key distribution structure written successfully.");
  }

  private static void clearDirectory(File directory) throws IOException {
    FileUtils.deleteDirectory(directory);
    directory.mkdirs();
  }
}
