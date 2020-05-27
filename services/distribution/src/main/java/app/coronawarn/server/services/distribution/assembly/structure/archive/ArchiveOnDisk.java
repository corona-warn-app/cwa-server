/*
 * Corona-Warn-App
 *
 * SAP SE and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package app.coronawarn.server.services.distribution.assembly.structure.archive;

import static app.coronawarn.server.services.distribution.assembly.structure.util.functional.CheckedConsumer.uncheckedConsumer;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.WritableOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.directory.DirectoryOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.file.FileOnDisk;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;
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
public class ArchiveOnDisk extends FileOnDisk implements Archive<WritableOnDisk> {

  private DirectoryOnDisk tempDirectory;

  /**
   * Constructs an {@link Archive} with an internal, temporary directory to store writables in.
   */
  public ArchiveOnDisk(String name) {
    super(name, new byte[0]);
    try {
      tempDirectory = new DirectoryOnDisk(
          Files.createTempDirectory("temporary").toFile());
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
  }

  @Override
  public byte[] getBytes() {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      this.getWritables().stream()
          .filter(writable -> writable instanceof File)
          .map(file -> (FileOnDisk) file)
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
}
