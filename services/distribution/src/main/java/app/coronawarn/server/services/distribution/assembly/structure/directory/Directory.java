

package app.coronawarn.server.services.distribution.assembly.structure.directory;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import java.util.Set;

/**
 * A {@link Writable} that can contains other {@link Writable Writables}.
 *
 * @param <W> The specific type of {@link Writable} that this {@link Directory} can be a child of.
 */
public interface Directory<W extends Writable<W>> extends Writable<W> {

  /**
   * Adds a {@link Writable} to this {@link Directory}.
   */
  void addWritable(Writable<W> writable);

  /**
   * Returns all {@link Writable writables} contained in this {@link Directory}.
   */
  Set<Writable<W>> getWritables();
}
