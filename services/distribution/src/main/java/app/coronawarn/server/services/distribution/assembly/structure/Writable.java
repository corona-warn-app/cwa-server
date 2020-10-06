

package app.coronawarn.server.services.distribution.assembly.structure;

import app.coronawarn.server.services.distribution.assembly.structure.archive.Archive;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import app.coronawarn.server.services.distribution.assembly.structure.util.ImmutableStack;

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
   */
  String getName();

  /**
   * Returns the parent of this {@link Writable}, or {@code null} if it doesn't have a parent.
   */
  Directory<W> getParent();

  /**
   * Sets the parent of this {@link Writable}.
   */
  void setParent(Directory<W> parent);

  /**
   * Does preparation work for this {@link Writable} (e.g. calculate data, setup structures, etc.).
   * Must be called before writing.
   */
  void prepare(ImmutableStack<Object> indices);

  /**
   * Returns whether this {@link Writable} is a {@link File}.
   */
  boolean isFile();

  /**
   * Returns whether this {@link Writable} is a {@link Directory}.
   */
  boolean isDirectory();

  /**
   * Returns whether this {@link Writable} is an {@link Archive}.
   */
  boolean isArchive();
}
