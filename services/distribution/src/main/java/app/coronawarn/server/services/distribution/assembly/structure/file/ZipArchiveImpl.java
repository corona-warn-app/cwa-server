package app.coronawarn.server.services.distribution.assembly.structure.file;

import static app.coronawarn.server.services.distribution.assembly.structure.functional.CheckedConsumer.uncheckedConsumer;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritablesContainer;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryImpl;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipArchiveImpl extends FileImpl implements Archive {

  private static final Logger logger = LoggerFactory.getLogger(ZipArchiveImpl.class);

  /**
   * how deep the folder structure will be scanned for files.
   */
  private static final int FILE_WALK_MAX_DEPTH = 3;

  private static final String TEMPORARY_DIRECTORY_NAME = "temporary";

  private Directory tempDirectory;

  public ZipArchiveImpl(String name) {
    super(name, new byte[0]);
    try {
      tempDirectory = new DirectoryImpl(
          Files.createTempDirectory(TEMPORARY_DIRECTORY_NAME).toFile());
    } catch (IOException e) {
      logger.error("Failed to create temporary directory for zip archive {}", this.getFileOnDisk());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setParent(WritablesContainer parent) {
    super.setParent(parent);
    tempDirectory.setParent(parent);
  }

  @Override
  public void addWritable(Writable writable) {
    this.tempDirectory.addWritable(writable);
  }

  @Override
  public Set<Writable> getWritables() {
    return this.tempDirectory.getWritables();
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.tempDirectory.prepare(indices);
    super.prepare(indices);
  }

  @Override
  public void write() {
    super.write();
    this.tempDirectory.write();
    try {
      zipDirectoryAndWriteArchive(this.tempDirectory);
    } catch (IOException e) {
      logger.error("Could not write archive {}", this.getFileOnDisk());
      throw new RuntimeException(e);
    }
  }

  private void zipDirectoryAndWriteArchive(Directory directory) throws IOException {
    FileOutputStream fileOutputStream = new FileOutputStream(this.getFileOnDisk().toString());
    ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
    Path directoryPath = directory.getFileOnDisk().toPath();
    Files.walk(directoryPath, FILE_WALK_MAX_DEPTH).forEach(uncheckedConsumer(path -> {
      if (path.toFile().isDirectory()) {
        return;
      }
      String pathInZip = path.toString().substring(directoryPath.toString().length() + 1);
      zipOutputStream.putNextEntry(new ZipEntry(pathInZip));
      byte[] bytes = Files.readAllBytes(path);
      zipOutputStream.write(bytes, 0, bytes.length);
    }));
    zipOutputStream.close();
    fileOutputStream.close();
  }
}
