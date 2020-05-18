package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Creates and clears a {@link Directory} on disk, which is defined by the application properties.
 */
@Component
public class OutputDirectoryProvider {

  private static final Logger logger = LoggerFactory.getLogger(OutputDirectoryProvider.class);

  @Value("${services.distribution.paths.output}")
  private String outputPath;

  public Directory getDirectory() {
    return new DirectoryImpl(getFileOnDisk());
  }

  public java.io.File getFileOnDisk() {
    return new java.io.File(outputPath);
  }

  /**
   * Removes the output directory and all files and directories within it.
   *
   * @throws IOException if file operation fails
   */
  public void clear() throws IOException {
    logger.debug("Clearing output directory...");
    java.io.File outputDirectory = getFileOnDisk();
    FileUtils.deleteDirectory(outputDirectory);
    outputDirectory.mkdirs();
  }
}
