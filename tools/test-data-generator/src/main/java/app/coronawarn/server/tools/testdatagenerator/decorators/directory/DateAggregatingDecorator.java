package app.coronawarn.server.tools.testdatagenerator.decorators.directory;

import app.coronawarn.server.common.protocols.internal.FileBucket;
import app.coronawarn.server.common.protocols.internal.SignedPayload;
import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.directory.decorator.DirectoryDecorator;
import app.coronawarn.server.services.distribution.structure.file.File;
import app.coronawarn.server.services.distribution.structure.file.FileImpl;
import app.coronawarn.server.services.distribution.structure.file.decorator.SigningDecorator;
import app.coronawarn.server.services.distribution.structure.functional.CheckedFunction;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link DirectoryDecorator} that will create a {@link FileBucket} for each day within its
 * directory.
 */
public class DateAggregatingDecorator extends DirectoryDecorator {

  public DateAggregatingDecorator(Directory directory) {
    super(directory);
  }

  @Override
  public void prepare(Stack<Object> indices) {
    super.prepare(indices);
    System.out.println("Aggregating \t\t" + this.getFileOnDisk().getPath());
    List<Directory> days = this.getDirectories();
    // Exclude the last day
    days.subList(0, days.size() - 1).forEach(currentDirectory -> {
      Stream.of(currentDirectory)
          .map(this::getSubDirectoryFiles)
          .map(this::parseFileBucketsFromFiles)
          .map(this::reduceFileBuckets)
          .map(this::makeNewFileBucket)
          .map(FileBucket::toByteArray)
          .map(bytes -> new FileImpl("index", bytes))
          .map(SigningDecorator::new)
          .peek(currentDirectory::addFile)
          .forEach(aggregate -> aggregate.prepare(indices));
    });
  }

  private List<File> getSubDirectoryFiles(Directory directory) {
    return Stream.of(directory)
        .map(Directory::getDirectories)
        .flatMap(List::stream)
        .map(Directory::getDirectories)
        .flatMap(List::stream)
        .map(Directory::getFiles)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private List<FileBucket> parseFileBucketsFromFiles(List<File> files) {
    return files.stream()
        .map(File::getBytes)
        .map(CheckedFunction.uncheckedFunction(SignedPayload::parseFrom))
        .map(SignedPayload::getPayload)
        .map(CheckedFunction.uncheckedFunction(FileBucket::parseFrom))
        .collect(Collectors.toList());
  }

  private List<app.coronawarn.server.common.protocols.external.exposurenotification.File> reduceFileBuckets(
      List<FileBucket> fileBuckets) {
    return fileBuckets.stream()
        .map(FileBucket::getFilesList)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private FileBucket makeNewFileBucket(
      List<app.coronawarn.server.common.protocols.external.exposurenotification.File> enFiles) {
    return FileBucket.newBuilder()
        .addAllFiles(enFiles)
        .build();
  }
}
