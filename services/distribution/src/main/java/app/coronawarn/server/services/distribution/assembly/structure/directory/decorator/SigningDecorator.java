package app.coronawarn.server.services.distribution.assembly.structure.directory.decorator;

public interface SigningDecorator {

  byte[] getBytesToSign();

  int getBatchNum();

  int getBatchSize();
}
