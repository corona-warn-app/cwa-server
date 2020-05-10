package app.coronawarn.server.tools.testdatagenerator.implementations;

import app.coronawarn.server.tools.testdatagenerator.interfaces.Directory;
import app.coronawarn.server.tools.testdatagenerator.interfaces.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A directory containing static files and further {@link Directory directories}.
 */
public class DirectoryImpl extends WritableImpl implements Directory {

  private final List<app.coronawarn.server.tools.testdatagenerator.interfaces.File> files = new ArrayList<>();
  private final List<Directory> directories = new ArrayList<>();

  /**
   * A root {@link DirectoryImpl} representing an already existing directory on disk.
   *
   * @param file The {@link File File} that this {@link DirectoryImpl} represents on disk.
   */
  public DirectoryImpl(java.io.File file) {
    super(file);
  }

  /**
   * A {@link DirectoryImpl} that does not yet represent an already existing directory on disk, but
   * one that shall be created on disk when calling {@link DirectoryImpl#write}. A parent needs to
   * be defined by calling {@link DirectoryImpl#setParent}, before writing can succeed.
   *
   * @param name The name that this directory should have on disk.
   */
  public DirectoryImpl(String name) {
    super(name);
  }

  /**
   * Adds a file to the {@link DirectoryImpl#files} of this {@link DirectoryImpl}.
   */
  public void addFile(File file) {
    this.files.add(file);
    file.setParent(this);
  }

  @Override
  public List<File> getFiles() {
    return this.files;
  }

  /**
   * Adds a {@link DirectoryImpl} to the {@link DirectoryImpl#directories} of this {@link
   * DirectoryImpl}.
   */
  public void addDirectory(Directory directory) {
    this.directories.add(directory);
    directory.setParent(this);
  }

  @Override
  public List<Directory> getDirectories() {
    return this.directories;
  }

  @Override
  public void prepare(Stack<Object> indices) {
    this.prepareFiles(indices);
    this.prepareDirectories(indices);
  }

  private void prepareDirectories(Stack<Object> indices) {
    this.directories.forEach(directory -> directory.prepare(indices));
  }

  private void prepareFiles(Stack<Object> indices) {
    this.files.forEach(file -> file.prepare(indices));
  }

  /**
   * Writes this {@link DirectoryImpl} and all of its {@link DirectoryImpl#files} and {@link
   * DirectoryImpl#directories} to disk.
   */
  public void write() {
    this.writeOwnDirectory();
    this.writeDirectories();
    this.writeFiles();
  }

  private void writeOwnDirectory() {
    java.io.File file = this.getFileOnDisk();
    if (file == null) {
      throw new RuntimeException(
          "No directory on disk has been defined.");
    }
    file.mkdirs();
  }

  private void writeDirectories() {
    this.directories.forEach(Directory::write);
  }

  private void writeFiles() {
    this.files.forEach(File::write);
  }
}
