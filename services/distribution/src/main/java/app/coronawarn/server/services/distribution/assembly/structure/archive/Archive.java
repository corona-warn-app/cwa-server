

package app.coronawarn.server.services.distribution.assembly.structure.archive;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;

/**
 * A {@link Writable} that can contains other {@link Writable Writables} and of which the bytes can be requested.
 *
 * @param <W> The specific type of {@link Writable} that this {@link Archive} can be a child of.
 */
public interface Archive<W extends Writable<W>> extends File<W>, Directory<W> {

}
