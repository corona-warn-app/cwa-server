package app.coronawarn.server.tools.testdatagenerator.decorators.directory;

import app.coronawarn.server.tools.testdatagenerator.interfaces.Directory;
import app.coronawarn.server.tools.testdatagenerator.interfaces.File;
import java.util.List;
import java.util.Stack;

public abstract class DirectoryDecorator implements Directory {

  private final Directory directory;

  protected DirectoryDecorator(Directory directory) {
    this.directory = directory;
  }

  @Override
  public void prepare(Stack<Object> indices) {
    this.directory.prepare(indices);
  }

  @Override
  public void addFile(app.coronawarn.server.tools.testdatagenerator.interfaces.File file) {
    this.directory.addFile(file);
  }

  @Override
  public List<File> getFiles() {
    return this.directory.getFiles();
  }

  @Override
  public void addDirectory(Directory directory) {
    this.directory.addDirectory(directory);
  }

  @Override
  public List<Directory> getDirectories() {
    return this.directory.getDirectories();
  }

  @Override
  public void write() {
    this.directory.write();
  }

  @Override
  public String getName() {
    return this.directory.getName();
  }

  @Override
  public Directory getParent() {
    return this.directory.getParent();
  }

  @Override
  public void setParent(Directory parent) {
    this.directory.setParent(parent);
  }

  @Override
  public java.io.File getFileOnDisk() {
    return this.directory.getFileOnDisk();
  }

}
