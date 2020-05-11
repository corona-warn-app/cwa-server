package app.coronawarn.server.tools.testdatagenerator.interfaces;

public interface File extends Writable {

  byte[] getBytes();

  void setBytes(byte[] bytes);
}
