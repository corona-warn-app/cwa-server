package app.coronawarn.server.tools.testdatagenerator.structure;

import app.coronawarn.server.tools.testdatagenerator.util.IOUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

/**
 * A directory containing static files and further {@link Directory directories}.
 */
public class Directory {

  private final Map<String, byte[]> files = new HashMap<>();
  private final List<Directory> directories = new ArrayList<>();
  private File file;
  private String name;
  private Directory parent;

  /**
   * A root {@link Directory} representing an already existing directory on disk.
   *
   * @param file The {@link File File} that this {@link Directory} represents on disk.
   */
  public Directory(File file) {
    this.file = file;
  }

  /**
   * A {@link Directory} that does not yet represent an already existing directory on disk, but one
   * that shall be created on disk when calling {@link Directory#write}. A {@link Directory#parent}
   * needs to be defined by calling {@link Directory#setParent}, before writing can succeed.
   *
   * @param name The name that this directory should have on disk.
   */
  public Directory(String name) {
    this.name = name;
  }

  public Directory getParent() {
    return parent;
  }

  public void setParent(Directory parent) {
    this.parent = parent;
  }

  public File getFile() {
    return Objects.requireNonNullElseGet(this.file,
        () -> getParent().getFile().toPath().resolve(this.name).toFile());
  }

  public Map<String, byte[]> getFiles() {
    return this.files;
  }

  /**
   * Adds a file to the {@link Directory#files} of this {@link Directory}.
   *
   * @return self
   */
  public Directory addFile(String name, byte[] bytes) {
    this.files.put(name, bytes);
    return this;
  }

  public List<Directory> getDirectories() {
    return this.directories;
  }

  /**
   * Adds a {@link Directory} to the {@link Directory#directories} of this {@link Directory}.
   *
   * @return self
   */
  public Directory addDirectory(Directory directory) {
    this.directories.add(directory);
    directory.setParent(this);
    return this;
  }

  /**
   * See {@link Directory#write(Stack)}. This method starts a new index stack.
   */
  public void write() {
    write(new Stack<>());
  }

  /**
   * Writes this {@link Directory} and all of its {@link Directory#files} to disk (by calling {@link
   * Directory#writeFiles}), then writes all of its {@link Directory#directories} to disk (by
   * calling {@link Directory#writeDirectories(Stack)}).
   */
  public void write(Stack<Object> indices) {
    this.writeOwnDirectory();
    this.writeDirectories(indices);
    this.writeFiles(indices);
  }

  protected void writeOwnDirectory() {
    File file;
    if (this.file == null) {
      if (this.parent == null) {
        throw new RuntimeException(
            "Neither a parent directory nor a directory on disk have been defined.");
      } else {
        file = IOUtils.makeDirectory(parent.getFile(), this.name);
      }
    } else {
      file = this.getFile();
    }
    file.mkdirs();
  }

  /**
   * Write all files of this {@link Directory} to disk. If this is an {@link AggregatingDirectory},
   * {@link AggregatingDirectory#aggregate} will be called. If this is a {@link SigningDirectory},
   * {@link SigningDirectory#sign} will be called.
   */
  protected void writeFiles(Stack<Object> indices) {
    if (this instanceof AggregatingDirectory) {
      ((AggregatingDirectory) this).aggregate();
    }
    if (this instanceof SigningDirectory) {
      ((SigningDirectory) this).sign();
    }
    this.files.forEach((filename, bytes) -> IOUtils
        .writeBytesToFile(bytes, IOUtils.makeFile(this.getFile(), filename)));
  }

  /**
   * Calls {@link Directory#write(Stack)} on all {@link Directory#directories}.
   */
  protected void writeDirectories(Stack<Object> indices) {
    this.directories.forEach(directory -> directory.write(indices));
  }
}
