package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

import app.coronawarn.server.services.distribution.assembly.component.CryptoProvider;
import app.coronawarn.server.services.distribution.assembly.structure.Writable;
import app.coronawarn.server.services.distribution.assembly.structure.directory.Directory;
import app.coronawarn.server.services.distribution.assembly.structure.file.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface SigningDecorator<W extends Writable<W>> extends Directory<W> {

  Logger logger = LoggerFactory.getLogger(SigningDecorator.class);

  File<W> getSignatureFile(String signatureFileName, CryptoProvider cryptoProvider);

  byte[] getBytesToSign();

  int getBatchNum();

  int getBatchSize();
}
