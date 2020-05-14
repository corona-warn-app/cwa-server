package app.coronawarn.server.services.distribution.common;

import app.coronawarn.server.services.distribution.structure.util.ImmutableStack;
import app.coronawarn.server.services.distribution.structure.directory.Directory;

public class Helpers {

  public static void prepareAndWrite(Directory directory) {
    directory.prepare(new ImmutableStack<>());
    directory.write();
  }

}
