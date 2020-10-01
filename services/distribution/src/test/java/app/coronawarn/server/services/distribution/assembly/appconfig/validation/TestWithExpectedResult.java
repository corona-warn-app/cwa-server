

package app.coronawarn.server.services.distribution.assembly.appconfig.validation;

public class TestWithExpectedResult {

  private final String prefixPath;

  public final String file;

  public final ValidationResult result = new ValidationResult();

  public TestWithExpectedResult(String file) {
    this(file, "parameters/");
  }

  public TestWithExpectedResult(String file, String prefixPath) {
    this.file = file;
    this.prefixPath = prefixPath;
  }


  public TestWithExpectedResult with(ValidationError error) {
    this.result.add(error);
    return this;
  }

  public String path() {
    return prefixPath + file;
  }

  @Override
  public String toString() {
    return file;
  }

  public static class Builder {

    private String folder;

    public Builder(String folder) {
      this.folder = folder;
    }

    public TestWithExpectedResult build(String file) {
      return new TestWithExpectedResult(file, this.folder);
    }
  }
}