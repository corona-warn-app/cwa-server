package app.coronawarn.server.services.distribution.structure;

import app.coronawarn.server.services.distribution.structure.directory.Directory;
import java.util.Stack;

public interface Writable {

  void write();

  String getName();

  Directory getParent();

  void setParent(Directory parent);

  java.io.File getFileOnDisk();

  void prepare(Stack<Object> indices);
}
