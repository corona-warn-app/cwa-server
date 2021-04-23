

package app.coronawarn.server.services.distribution.assembly.structure.archive;

import static app.coronawarn.server.common.shared.functional.CheckedConsumer.uncheckedConsumer;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDiskWithChecksum;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An {@link Archive} that can be written to disk as a ZIP archive.
 */
public class ArchiveOnDisk extends FileOnDiskWithChecksum implements Archive<WritableOnDisk> {

  private DirectoryOnDisk tempDirectory;

  /**
   * The checksum-relevant content of this {@link ArchiveOnDisk}.
   */
  private byte[] bytesForChecksum;

  /**
   * Constructs an {@link Archive} with an internal, temporary directory to store writables in.
   *
   * @param name name of directory
   */
  public ArchiveOnDisk(String name) {
    super(name, new byte[0]);
    try {
      tempDirectory = new DirectoryOnDisk(Files.createTempDirectory("temporary").toFile());
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create temporary directory for zip archive " + this.getFileOnDisk(), e);
    }
  }

  @Override
  public void setParent(Directory<WritableOnDisk> parent) {
    super.setParent(parent);
    tempDirectory.setParent(parent);
  }

  @Override
  public void addWritable(Writable<WritableOnDisk> writable) {
    this.tempDirectory.addWritable(writable);
  }

  @Override
  public Set<Writable<WritableOnDisk>> getWritables() {
    return this.tempDirectory.getWritables();
  }

  @Override
  public void prepare(ImmutableStack<Object> indices) {
    this.tempDirectory.prepare(indices);

    updateBytesForChecksum();
  }

  @Override
  public byte[] getBytes() {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      this.getWritables().stream()
          .filter(writable -> writable instanceof File)
          .map(FileOnDisk.class::cast)
          .forEach(uncheckedConsumer(file -> {
            String pathInZip = file.getName();
            zipOutputStream.putNextEntry(new ZipEntry(pathInZip));
            byte[] bytes = file.getBytes();
            zipOutputStream.write(bytes, 0, bytes.length);
          }));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close zip archive output stream.", e);
    }
    return byteArrayOutputStream.toByteArray();
  }

  @Override
  public void setBytes(byte[] bytes) {
    throw new UnsupportedOperationException("Can not set bytes on an archive.");
  }

  private void updateBytesForChecksum() {
    var targetFile = this.getWritables().stream()
        .filter(Writable::isFile)
        .map(FileOnDisk.class::cast)
        .findFirst()
        .orElseThrow();

    this.bytesForChecksum = targetFile.getBytes();
  }

  @Override
  protected byte[] getBytesForChecksum() {
    return this.bytesForChecksum;
  }
}
