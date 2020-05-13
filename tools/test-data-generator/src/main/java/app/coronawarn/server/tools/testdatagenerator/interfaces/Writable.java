package app.coronawarn.server.tools.testdatagenerator.interfaces;

import java.util.Stack;

public interface Writable {

  void write();

  String getName();

  Directory getParent();

  void setParent(Directory parent);

  java.io.File getFileOnDisk();

  void prepare(Stack<Object> indices);
}
