package app.coronawarn.server.services.distribution.structure.file;

import app.coronawarn.server.services.distribution.structure.Writable;

public interface File extends Writable {

  byte[] getBytes();

  void setBytes(byte[] bytes);
}
