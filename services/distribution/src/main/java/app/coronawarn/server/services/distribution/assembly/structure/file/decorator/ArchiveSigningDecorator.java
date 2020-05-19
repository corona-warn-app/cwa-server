package app.coronawarn.server.services.distribution.assembly.structure.file.decorator;

import app.coronawarn.server.services.distribution.assembly.structure.file.Archive;

public interface ArchiveSigningDecorator extends Archive {

  byte[] getBytesToSign();

  int getBatchNum();

  int getBatchSize();
}
