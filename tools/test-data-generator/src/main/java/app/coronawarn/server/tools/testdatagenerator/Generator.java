package app.coronawarn.server.tools.testdatagenerator;

import app.coronawarn.server.tools.testdatagenerator.structure.Directory;
import app.coronawarn.server.tools.testdatagenerator.structure.IndexDirectory;
import app.coronawarn.server.tools.testdatagenerator.structure.cwa.diagnosiskeys.DiagnosisKeysDirectory;
import app.coronawarn.server.tools.testdatagenerator.structure.cwa.parameters.ParametersDirectory;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import app.coronawarn.server.tools.testdatagenerator.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    Crypto crypto = new Crypto(privateKeyFile, certificateFile);
    LocalDate startDate = LocalDate.parse(startDateStr, ISO8601);

    DiagnosisKeysDirectory diagnosisKeysDirectory = new DiagnosisKeysDirectory(startDate,
        totalHours, exposuresPerHour, COUNTRY, ISO8601, crypto, random);
    ParametersDirectory parametersDirectory = new ParametersDirectory(COUNTRY, crypto);

    IndexDirectory<?> versionDirectory = new IndexDirectory<>("version", __ -> List.of(VERSION));
    if (openapi != null) {
      versionDirectory.addFileToAll("index", __ -> {
        try {
          return IOUtils.getBytesFromFile(openapi);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    versionDirectory.addDirectoryToAll(diagnosisKeysDirectory);
    versionDirectory.addDirectoryToAll(parametersDirectory);

    Directory root = new Directory(outputDirectory);
    root.addDirectory(versionDirectory);
    root.write();

    System.out.println("DONE");
  }

  private static void clearDirectory(File directory) throws IOException {
    FileUtils.deleteDirectory(directory);
    //noinspection ResultOfMethodCallIgnored
    directory.mkdirs();
  }
}
