package app.coronawarn.server.tools.testdatagenerator;

import app.coronawarn.server.services.distribution.crypto.Crypto;
import app.coronawarn.server.services.distribution.io.IO;
import app.coronawarn.server.services.distribution.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectory;
import app.coronawarn.server.services.distribution.structure.directory.IndexDirectoryImpl;
import app.coronawarn.server.services.distribution.structure.directory.decorator.IndexingDecorator;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.tools.testdatagenerator.implementations.cwa.DiagnosisKeysDirectoryImpl;
import app.coronawarn.server.tools.testdatagenerator.implementations.cwa.ParametersDirectoryImpl;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Stack;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

public class Generator {

  private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String COUNTRY = "DE";
  private static final String VERSION = "v1";

  /**
   * See {@link GenerateCommand}.
   */
  static void generate(int totalHours, String startDateStr, int exposuresPerHour, File openapi,
      File outputDirectory, File privateKeyFile, File certificateFile, int seed)
      throws IOException, CertificateException {

    clearDirectory(outputDirectory);

    RandomGenerator random = new JDKRandomGenerator();
    random.setSeed(seed);
    // Crypto crypto = new Crypto(privateKeyFile, certificateFile);
    LocalDate startDate = LocalDate.parse(startDateStr, ISO8601);

    DiagnosisKeysDirectoryImpl diagnosisKeysDirectory = new DiagnosisKeysDirectoryImpl(startDate,
        totalHours, exposuresPerHour, COUNTRY, ISO8601, random);
    ParametersDirectoryImpl parametersDirectory = new ParametersDirectoryImpl(COUNTRY);

    IndexDirectory<?> versionDirectory =
        new IndexDirectoryImpl<>("version", __ -> List.of(VERSION));
    if (openapi != null) {
      versionDirectory.addFileToAll(__ -> {
        try {
          return new FileImpl("index", IO.getBytesFromFile(openapi));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    versionDirectory.addDirectoryToAll(__ -> diagnosisKeysDirectory);
    versionDirectory.addDirectoryToAll(__ -> parametersDirectory);

    DirectoryImpl root = new DirectoryImpl(outputDirectory);
    root.addDirectory(new IndexingDecorator<>(versionDirectory));
    System.out.println("Generating ...");
    root.prepare(new Stack<>());
    System.out.println("Writing ...");
    root.write();

    System.out.println("DONE");
  }

  private static void clearDirectory(File directory) throws IOException {
    FileUtils.deleteDirectory(directory);
    directory.mkdirs();
  }
}
