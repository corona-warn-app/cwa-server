package app.coronawarn.server.services.distribution.structure;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;

/**
 * Something that can be written to disk.
 */
public interface Writable {

  /**
   * Writes this {@link Writable} to disk.
   */
  void write();

  /**
   * Returns the name of this {@link Writable}.
   */
  String getName();

  /**
   * Returns the parent of this {@link Writable}, or {@code null} if it doesn't have a parent.
   */
  Directory getParent();

  /**
   * Sets the parent of this {@link Writable}.
   */
  void setParent(Directory parent);

  /**
   * Returns the {@link java.io.File} that this {@link Writable} represents on disk.
   */
  java.io.File getFileOnDisk();

  /**
   * Does preparation work for this {@link Writable} (e.g. calculate data, setup structures, etc.).
   */
  void prepare(ImmutableStack<Object> indices);
}
