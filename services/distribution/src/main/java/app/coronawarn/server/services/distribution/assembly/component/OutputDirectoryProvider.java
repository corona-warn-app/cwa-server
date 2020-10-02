

package app.coronawarn.server.services.distribution.assembly.component;

import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.config.DistributionServiceConfig;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Creates and clears a {@link Directory} on disk, which is defined by the application properties.
 */
@Component
public class OutputDirectoryProvider {

  private static final Logger logger = LoggerFactory.getLogger(OutputDirectoryProvider.class);
  private final String outputPath;

  OutputDirectoryProvider(DistributionServiceConfig distributionServiceConfig) {
    this.outputPath = distributionServiceConfig.getPaths().getOutput();
  }

  public Directory<WritableOnDisk> getDirectory() {
    return new DirectoryOnDisk(getFileOnDisk());
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
    if (!outputDirectory.mkdirs()) {
      throw new IOException("Failed to clear output directory.");
    }
  }
}
