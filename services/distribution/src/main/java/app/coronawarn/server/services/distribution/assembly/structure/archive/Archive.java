package app.coronawarn.server.services.distribution.assembly.structure.archive;

import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;

public interface Archive<W extends Writable<W>> extends File<W>, Directory<W> {

}
