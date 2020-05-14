package app.coronawarn.server.services.distribution.diagnosiskeys.structure.directory.decorator;

import app.coronawarn.server.common.protocols.internal.FileBucket;
import app.coronawarn.server.common.protocols.internal.SignedPayload;
import app.coronawarn.server.services.distribution.crypto.CryptoProvider;
import app.coronawarn.server.services.distribution.diagnosiskeys.structure.file.HourFileImpl;
import app.coronawarn.server.services.distribution.structure.Writable;
import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.decorator.DirectoryDecorator;
import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.structure.file.decorator.SigningDecorator;
import app.coronawarn.server.services.distribution.structure.functional.CheckedFunction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DirectoryDecorator} that will create a {@link FileBucket} for each day within its
 * directory.
 */
public class DateAggregatingDecorator extends DirectoryDecorator {

  private static final Logger logger = LoggerFactory.getLogger(DateAggregatingDecorator.class);

  private final CryptoProvider cryptoProvider;

  private static final String AGGREGATE_FILE_NAME = "index";

  public DateAggregatingDecorator(Directory directory, CryptoProvider cryptoProvider) {
    super(directory);
    this.cryptoProvider = cryptoProvider;
  }

  @Override
  public void prepare(Stack<Object> indices) {
    super.prepare(indices);
    logger.debug("Aggregating {}", this.getFileOnDisk().getPath());
    Set<Directory> days = this.getDirectories();
    if (days.size() == 0) {
      return;
    }

    List<Directory> sortedDays = new ArrayList<>(days);
    sortedDays.sort(Comparator.comparing(Writable::getName));

    // Exclude the last day
    sortedDays.subList(0, days.size() - 1).forEach(currentDirectory -> {
      Stream.of(currentDirectory)
          .map(this::getSubDirectoryFiles)
          .map(this::parseFileBucketsFromFiles)
          .map(this::reduceFileBuckets)
          .map(this::makeNewFileBucket)
          .map(FileBucket::toByteArray)
          .map(bytes -> new FileImpl(AGGREGATE_FILE_NAME, bytes))
          .map(file -> new SigningDecorator(file, cryptoProvider))
          .peek(currentDirectory::addFile)
          .forEach(aggregate -> aggregate.prepare(indices));
    });
  }

  private Set<File> getSubDirectoryFiles(Directory directory) {
    return Stream.of(directory)
        .map(Directory::getDirectories)
        .flatMap(Set::stream)
        .map(Directory::getDirectories)
        .flatMap(Set::stream)
        .map(Directory::getFiles)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  private Set<FileBucket> parseFileBucketsFromFiles(Set<File> files) {
    return files.stream()
        .map(File::getBytes)
        .map(CheckedFunction.uncheckedFunction(SignedPayload::parseFrom))
        .map(SignedPayload::getPayload)
        .map(CheckedFunction.uncheckedFunction(FileBucket::parseFrom))
        .collect(Collectors.toSet());
  }

  private Set<app.coronawarn.server.common.protocols.external.exposurenotification.File> reduceFileBuckets(
      Set<FileBucket> fileBuckets) {
    return fileBuckets.stream()
        .map(FileBucket::getFilesList)
        .flatMap(List::stream)
        .collect(Collectors.toSet());
  }

  private FileBucket makeNewFileBucket(
      Set<app.coronawarn.server.common.protocols.external.exposurenotification.File> enFiles) {
    return FileBucket.newBuilder()
        .addAllFiles(enFiles)
        .build();
  }
}
