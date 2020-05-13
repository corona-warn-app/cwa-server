package app.coronawarn.server.services.distribution.common;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import java.util.Stack;

public class Helpers {

  public static void prepareAndWrite(Directory directory) {
    directory.prepare(new Stack<>());
    directory.write();
  }

}
