package app.coronawarn.server.services.distribution.assembly.structure.file;

import app.coronawarn.server.services.distribution.assembly.structure.WritablesContainer;

/**
 * A {@link File} that can contain other {@link WritablesContainer writables}.
 */
public interface Archive extends File, WritablesContainer {

}
