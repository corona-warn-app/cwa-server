package app.coronawarn.server.services.distribution.assembly.structure;

import app.coronawarn.server.common.shared.collection.ImmutableStack;
import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;

/**
 * Something that has a name, a parent and that can be written to somewhere.
 *
 * @param <W> The specific type of {@link Writable} that this {@link Writable} can be a child of.
 */
public interface Writable<W extends Writable<W>> {

  /**
   * Writes this {@link Writable} somewhere.
   */
  void write();

  /**
   * Returns the name of this {@link Writable}.
   *
   * @return name
   */
  String getName();

  /**
   * Returns the parent of this {@link Writable}, or {@code null} if it doesn't have a parent.
   *
   * @return Directory generic type
   */
  Directory<W> getParent();

  /**
   * Sets the parent of this {@link Writable}.
   *
   * @param parent Directory generic type
   */
  void setParent(Directory<W> parent);

  /**
   * Does preparation work for this {@link Writable} (e.g. calculate data, setup structures, etc.).
   * Must be called before writing.
   *
   * @param indices stack
   */
  void prepare(ImmutableStack<Object> indices);

  /**
   * Returns whether this {@link Writable} is a {@link File}.
   *
   * @return if file is writable
   */
  boolean isFile();

  /**
   * Returns whether this {@link Writable} is a {@link Directory}.
   *
   * @return if directory is writable
   */
  boolean isDirectory();

  /**
   * Returns whether this {@link Writable} is an {@link Archive}.
   *
   * @return if archive is writable
   */
  boolean isArchive();
}
