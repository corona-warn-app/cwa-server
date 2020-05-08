package app.coronawarn.server.tools.testdatagenerator.generate;

import app.coronawarn.server.tools.testdatagenerator.common.Common.Formatter;
import app.coronawarn.server.tools.testdatagenerator.common.IOUtils;
import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.simple.JSONArray;

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

    // FixedDirectory???
    /*
    if (openapi != null && openapi.exists()) {
      File target = IOUtils
          .makeFile(rootDirectory, "index");
      Files.copy(openapi.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
     */

    Random random = new Random(seed);
    Crypto crypto = new Crypto(privateKeyFile, certificateFile);
    LocalDate startDate = LocalDate.parse(startDateStr, ISO8601);

    Directory<?> root = new Directory<>(outputDirectory)
        .addSubDirectory(new IndexDirectory<>("version", List.of(VERSION))
            .addSubDirectory(new DiagnosisKeyDirectory(startDate, totalHours, crypto, random))
            .addSubDirectory(new ParametersDirectory())
        );
    root.write();

    System.out.println("DONE");
  }

  /**
   * @param hours
   * @return The number of days covered by {@code hours} (rounded up) Examples: {@code
   * (assert(getNumberOfDays(23) == 1); assert(getNumberOfDays(24) == 1); assert(getNumberOfDays(25)
   * == 2);}.
   */
  static int getNumberOfDays(int hours) {
    return -Math.floorDiv(-hours, 24);
  }

  /**
   * @param startDate
   * @param numDays
   * @return A list of all {@link LocalDate LocalDates} between {@code startDate} and {@code
   * numDays} later.
   */
  static List<LocalDate> getDates(LocalDate startDate, int numDays) {
    return IntStream.range(0, numDays)
        .mapToObj(startDate::plusDays)
        .collect(Collectors.toList());
  }

  /**
   * @param startDate
   * @param currentDate
   * @param totalHours
   * @return A list of all {@link LocalDateTime LocalDateTimes} between {@code startDate} and {@code
   * currentDate} (at 00:00 UTC) plus {@code totalHours % 24}.
   */
  static List<LocalDateTime> getHours(LocalDate startDate, LocalDate currentDate, int totalHours) {
    int numFullDays = Math.floorDiv(totalHours, 24);
    long currentDay = ChronoUnit.DAYS.between(startDate, currentDate);
    int lastHour = (currentDay < numFullDays) ? 24 : totalHours % 24;
    return IntStream.range(0, lastHour)
        .mapToObj(hour -> currentDate.atStartOfDay().plusHours(hour))
        .collect(Collectors.toList());
  }

  /**
   * A directory containing static files and further {@link Directory directories}.
   *
   * @param <T> The type of {@link Directory directories} that are allowed as subdirectories.
   */
  private static class Directory<T extends Directory<?>> {

    protected String name;
    protected File directory;
    private Directory<?> parent;
    protected Map<String, byte[]> staticFiles = new HashMap<>();
    protected List<T> subDirectories = new ArrayList<>();

    /**
     * @param directory The {@link File File} that this {@link Directory} represents on disk.
     */
    public Directory(File directory) {
      this.directory = directory;
    }

    /**
     * @param name The name of this directory on disk (directly underneath the parent directory).
     */
    public Directory(String name) {
      this.name = name;
    }

    /**
     * Assigns the {@link Directory#parent} of this {@link Directory}.
     *
     * @param parent The parent of this directory.
     */
    public void setParent(Directory<?> parent) {
      this.parent = parent;
    }

    /**
     * Writes this directory and all its static files to disk and calls {@link Directory#write} on
     * all {@link Directory#subDirectories}.
     */
    public void write() {
      this.writeDirectory();
      this.writeStaticFiles();
      this.subDirectories.forEach(Directory::write);
    }

    /**
     * Adds an element to the {@link Directory#subDirectories}.
     *
     * @param subDirectory The {@link Directory} to be added to this one.
     * @return self
     */
    public Directory<T> addSubDirectory(T subDirectory) {
      this.subDirectories.add(subDirectory);
      subDirectory.setParent(this);
      return this;
    }

    private void writeDirectory() {
      if (this.directory == null) {
        if (this.parent == null) {
          throw new RuntimeException(
              "Neither a parent directory nor a directory on disk have been defined.");
        } else {
          this.directory = IOUtils.makeDirectory(parent.directory, this.name);
        }
      }
      this.directory.mkdirs();
    }

    private void writeStaticFiles() {
      this.staticFiles.forEach((filename, bytes) -> IOUtils
          .writeBytesToFile(bytes, IOUtils.makeFile(this.directory, filename)));
    }
  }

  /**
   * A meta directory that maps its on-disk subdirectories to a list of elements (the {@link
   * IndexDirectory#index}). Operations performed on this class are performed on all of its
   * subdirectories transparently.
   *
   * @param <T> The type of the elements in the index (e.g. LocalDate for the /date directory)
   * @param <R> The type of {@link Directory directories} that are allowed as subdirectories.
   */
  private static class IndexDirectory<T, R extends Directory<?>> extends Directory<R> {

    private List<T> index;
    private Function<T, String> indexFormatter;

    /**
     * @param name           {@link Directory#name}
     * @param index          A list of elements that form the index of this aggregation level (e.g.
     *                       a list of {@link LocalDate LocalDates} for the {@code /date} path.
     * @param indexFormatter A {@link Formatter} used to format the directory name that each of the
     *                       elements of the {@link IndexDirectory#index} correspond to.
     */
    public IndexDirectory(String name, List<T> index, Formatter<T> indexFormatter) {
      super(name);
      this.index = index;
      this.indexFormatter = indexFormatter;
    }

    /**
     * Constructor that defaults the {@link IndexDirectory#indexFormatter} to {@link
     * Object#toString}
     *
     * @param name  {@link Directory#name}
     * @param index {@link IndexDirectory#index}
     */
    public IndexDirectory(String name, List<T> index) {
      this(name, index, Object::toString);
    }

    /**
     * @return {@link IndexDirectory#index}
     */
    public List<T> getIndex() {
      return this.index;
    }

    /**
     * Creates a new directory for each element of {@link IndexDirectory#index}, formatted with the
     * {@link IndexDirectory#indexFormatter} and calls {@link Directory#write} on all {@link
     * Directory#subDirectories}.
     */
    public void write() {
      super.write();
      this.writeIndex();
      this.writeMetaDirectories();
    }

    /**
     * Creates a directory for every element of the {@link IndexDirectory#index}, formats its name
     * with the {@link IndexDirectory#indexFormatter}, assigns this {@link Directory} as its {@link
     * Directory#parent} and calls each directories {@link Directory#write} function.
     */
    private void writeMetaDirectories() {
      this.index.stream()
          .map(this.indexFormatter)
          .map(Directory::new)
          .peek(directory -> directory.setParent(this))
          .peek(Directory::write)
          .peek(directory -> {
            if (directory instanceof SigningDirectory) {

            }
          })

      ;
    }

    /**
     * Writes a file called {@code "index"}, containing a JSON String of an array containing all
     * elements of {@link IndexDirectory#index}, formatted with the {@link
     * IndexDirectory#indexFormatter}.
     */
    @SuppressWarnings("unchecked")
    private void writeIndex() {
      File file = IOUtils.makeFile(this.directory, "index");
      JSONArray array = new JSONArray();
      List<?> elements = this.index.stream()
          .map(this.indexFormatter)
          .collect(Collectors.toList());
      array.addAll(elements);
      IOUtils.writeJson(file, array);
    }
  }

  /**
   * A {@link Directory} that will convert all of its {@link Directory#staticFiles} into {@link
   * app.coronawarn.server.common.protocols.internal.SignedPayload SignedPayloads} before writing
   * them.
   */
  private interface SigningDirectory {
    //TODO
  }

  /**
   * An {@link IndexDirectory} that, before writing, adds a file to its {@link
   * Directory#staticFiles} that aggregates the contents of all of its subdirectories.
   */
  private static abstract class AggregatedIndexDirectory {
    //TODO
  }

  /**
   * See {@link SigningDirectory}.
   */
  private static abstract class SignedDirectory<T, R extends Directory<?>> extends
      Directory<R> implements SigningDirectory {

    public SignedDirectory(String name) {
      super(name);
    }
    //TODO
  }

  /**
   * See {@link SigningDirectory} amd {@link IndexDirectory}.
   */
  private static abstract class SignedIndexDirectory<T, R extends Directory<?>> extends
      IndexDirectory<T, R> implements SigningDirectory {

    private Crypto crypto;

    public SignedIndexDirectory(String name, List<T> index,
        Formatter<T> indexFormatter, Crypto crypto) {
      super(name, index, indexFormatter);
      this.crypto = crypto;
      // TODO
    }
    //TODO
  }

  private static class DiagnosisKeyDirectory extends Directory<Directory<?>> {

    private static class DateIndexDirectory extends
        SignedIndexDirectory<LocalDate, HourIndexDirectory> {

      public DateIndexDirectory(LocalDate startDate, int totalHours, Crypto crypto) {
        super("date", getDates(startDate, getNumberOfDays(totalHours)), ISO8601::format, crypto);
        // TODO
      }
    }

    private static class HourIndexDirectory extends
        SignedIndexDirectory<LocalDateTime, Directory<?>> {

      private final Random random;

      public HourIndexDirectory(LocalDate startDate, LocalDate currentDate, int totalHours,
          Crypto crypto, Random random) {
        super("hour", getHours(startDate, currentDate, totalHours),
            // TODO No strings here, ints are fine
            hour -> String.valueOf(hour.getHour()), crypto);
        this.random = random;
        // TODO
      }
    }

    public DiagnosisKeyDirectory(LocalDate startDate, int totalHours, Crypto crypto,
        Random random) {
      super("diagnosis-keys");
      DateIndexDirectory dateDirectory = new DateIndexDirectory(startDate, totalHours, crypto);
      dateDirectory.getIndex().forEach(currentDate -> {
        dateDirectory.addSubDirectory(
            new HourIndexDirectory(startDate, currentDate, totalHours, crypto, random)
        );
      });
      this.addSubDirectory(new IndexDirectory<>("country", List.of(COUNTRY))
          .addSubDirectory(dateDirectory)
      );
      //TODO
    }
  }

  private static class ParametersDirectory extends SignedDirectory {

    public ParametersDirectory() {
      super("parameters");
      this.addSubDirectory(new IndexDirectory<>("country", List.of(COUNTRY)));
      //TODO
      /*

    // Write parameters
    File parametersDirectory = createParametersDirectoryStructure(rootDirectory);
    File parametersFile = IOUtils
        .makeFile(parametersDirectory, "index");
    RiskScoreParameters riskScoreParameters = RiskScoreParameters.newBuilder()
        .setAttenuation(AttenuationRiskParameters.newBuilder()
            .setGt73Dbm(RiskLevel.RISK_LEVEL_LOWEST)
            .setGt63Le73Dbm(RiskLevel.RISK_LEVEL_LOW)
            .setGt51Le63Dbm(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setGt33Le51Dbm(RiskLevel.RISK_LEVEL_MEDIUM)
            .setGt27Le33Dbm(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setGt10Le15Dbm(RiskLevel.RISK_LEVEL_HIGH)
            .setGt10Le15Dbm(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setLt10Dbm(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .setDaysSinceLastExposure(DaysSinceLastExposureRiskParameters.newBuilder()
            .setGe14Days(RiskLevel.RISK_LEVEL_LOWEST)
            .setGe12Lt14Days(RiskLevel.RISK_LEVEL_LOW)
            .setGe10Lt12Days(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setGe8Lt10Days(RiskLevel.RISK_LEVEL_MEDIUM)
            .setGe6Lt8Days(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setGe4Lt6Days(RiskLevel.RISK_LEVEL_HIGH)
            .setGe2Lt4Days(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setGe0Lt2Days(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .setDuration(DurationRiskParameters.newBuilder()
            .setEq0Min(RiskLevel.RISK_LEVEL_LOWEST)
            .setGt0Le5Min(RiskLevel.RISK_LEVEL_LOW)
            .setGt5Le10Min(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setGt10Le15Min(RiskLevel.RISK_LEVEL_MEDIUM)
            .setGt15Le20Min(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setGt20Le25Min(RiskLevel.RISK_LEVEL_HIGH)
            .setGt25Le30Min(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setGt30Min(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .setTransmission(TransmissionRiskParameters.newBuilder()
            .setAppDefined1(RiskLevel.RISK_LEVEL_LOWEST)
            .setAppDefined2(RiskLevel.RISK_LEVEL_LOW)
            .setAppDefined3(RiskLevel.RISK_LEVEL_LOW_MEDIUM)
            .setAppDefined4(RiskLevel.RISK_LEVEL_MEDIUM)
            .setAppDefined5(RiskLevel.RISK_LEVEL_MEDIUM_HIGH)
            .setAppDefined6(RiskLevel.RISK_LEVEL_HIGH)
            .setAppDefined7(RiskLevel.RISK_LEVEL_VERY_HIGH)
            .setAppDefined8(RiskLevel.RISK_LEVEL_HIGHEST)
            .build())
        .build();
    SignedPayload signedRiskScoreParameters = Signer.generateSignedPayload(
        riskScoreParameters.toByteArray(),
        privateKey,
        certificate);
    IOUtils
        .writeBytesToFile(signedRiskScoreParameters.toByteArray(), parametersFile);
      */
    }
  }

  private static class Crypto {

    private PrivateKey privateKey;
    private Certificate certificate;

    public Crypto(PrivateKey privateKey, Certificate certificate) {
      this.privateKey = privateKey;
      this.certificate = certificate;
    }

    public Crypto(File privateKeyFile, File certificateFile)
        throws IOException, CertificateException {
      this(IOUtils.getPrivateKeyFromFile(privateKeyFile),
          IOUtils.getCertificateFromFile(certificateFile));
    }
  }
}
