package app.coronawarn.server.tools.testdatagenerator.structure.cwa.diagnosiskeys;

import app.coronawarn.server.common.protocols.internal.FileBucket;
import app.coronawarn.server.common.protocols.internal.SignedPayload;
import app.coronawarn.server.tools.testdatagenerator.structure.AggregatingDirectory;
import app.coronawarn.server.tools.testdatagenerator.structure.IndexDirectory;
import app.coronawarn.server.tools.testdatagenerator.structure.SigningDirectory;
import app.coronawarn.server.tools.testdatagenerator.util.Common;
import app.coronawarn.server.tools.testdatagenerator.util.Crypto;
import app.coronawarn.server.tools.testdatagenerator.util.IOUtils;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math3.random.RandomGenerator;

class DateDirectory extends IndexDirectory<LocalDate> implements SigningDirectory,
    AggregatingDirectory {

  private final Crypto crypto;

  public DateDirectory(LocalDate startDate, int totalHours, int exposuresPerHour,
      DateTimeFormatter formatter, Crypto crypto, RandomGenerator random) {
    super("date", __ -> Common.getDates(startDate, Common.getNumberOfDays(totalHours)),
        formatter::format);
    this.crypto = crypto;
    this.addDirectoryToAll(
        new HourDirectory(startDate, totalHours, exposuresPerHour, crypto, random));
  }

  @Override
  public void sign() {
    Arrays.stream(Objects.requireNonNull(this.getFile().listFiles()))
        .forEach(file -> this.signFiles(file, this.crypto));
  }

  public void aggregate() {
    this.aggregateHours();
  }

  private void aggregateHours() {

    Arrays.stream(Objects.requireNonNull(this.getFile().listFiles()))
        .forEach(dateDirectory -> {
          FileBucket.Builder builder = FileBucket.newBuilder();
          Stream.of(dateDirectory)
              .map(File::listFiles).filter(Objects::nonNull)
              .flatMap(Arrays::stream) // Skip "/hour" intermediate directory
              .filter(File::isDirectory)
              .map(File::listFiles).filter(Objects::nonNull)
              .flatMap(Arrays::stream)
              .filter(File::isDirectory) // Skip the "index" file (containing a list of all hours)
              .map(hourDirectory -> Arrays.stream(Objects.requireNonNull(hourDirectory.listFiles()))
                  .findFirst()
                  .orElseThrow())
              .map(Common.uncheckedFunction(IOUtils::getBytesFromFile))
              .map(Common.uncheckedFunction(SignedPayload::parseFrom))
              .map(SignedPayload::getPayload)
              .map(Common.uncheckedFunction(FileBucket::parseFrom))
              .collect(Collectors.toList())
              .forEach(fileBucket -> builder.addAllFiles(fileBucket.getFilesList()));
          FileBucket aggregatedFileBucket = builder.build();
          File outputFile = IOUtils.makeFile(dateDirectory, "index");
          IOUtils.writeBytesToFile(aggregatedFileBucket.toByteArray(), outputFile);
        });
  }
}
